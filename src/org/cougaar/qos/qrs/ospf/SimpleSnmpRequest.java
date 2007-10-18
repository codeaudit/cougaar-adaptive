/*_############################################################################
  _##
  _##  SNMP4J - SnmpRequest.java
  _##
  _##  Copyright 2003-2007  Frank Fock and Jochen Katz (SNMP4J.org)
  _##
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##
  _##########################################################################*/

package org.cougaar.qos.qrs.ospf;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogLevel;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.DefaultCounterListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeListener;
import org.snmp4j.util.TreeUtils;

/**
 * Stripped down version of org.snmp4j.tools.console.SnmpRequest
 */
public class SimpleSnmpRequest {
    private final Logger log = Logging.getLogger(getClass());
    private final PDUv1 v1TrapPDU = new PDUv1();
    private final TimeTicks sysUpTime = new TimeTicks(0);
    private Vector<VariableBinding> vbs = new Vector<VariableBinding>();
    private Target target;
    private Address address;
    private int version = SnmpConstants.version3;
    private int engineBootCount = 0;
    private OID authProtocol;
    private OID privProtocol;
    private OID trapOID = SnmpConstants.coldStart;
    // private OID lowerBoundIndex, upperBoundIndex;
    private OctetString privPassphrase;
    private OctetString authPassphrase;
    private OctetString community = new OctetString("public");
    private OctetString securityName = new OctetString();
    private OctetString contextEngineID;
    private OctetString contextName = new OctetString();
    // private OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
    // private OctetString authoritativeEngineID;
    private int retries = 1;
    private int timeout = 1000;
    private int maxRepetitions = 10;
    private int nonRepeaters = 0;
    private int maxSizeResponsePDU = 65535;
    private int operation = SnmpRequest.DEFAULT;
    private int pduType = PDU.GETNEXT;
    // private boolean useDenseTableOperation = false;

    public SimpleSnmpRequest(String[] args, List<OID> nodes) {
        // Set the default counter listener to return proper USM and MP error
        // counters.
        CounterSupport.getInstance().addCounterListener(new DefaultCounterListener());
        
        if (nodes != null) {
            for (OID node : nodes) {
                vbs.add(new VariableBinding(node));
            }
        } else {
            vbs.add(new VariableBinding(new OID("1.3.6")));
        }
        int paramStart = parseArgs(args);
        if (paramStart >= args.length) {
            // printUsage();
            log.error("Weird argument parsing error");
            return;
        } else {
            checkOptions();
            address = getAddress(args[paramStart++]);
            Vector<VariableBinding> vbs = getVariableBindings(args, paramStart);
            checkTrapVariables(vbs);
            if (vbs.size() > 0) {
                this.vbs = vbs;
            }
        }
    }
    
    public void setOperation(int operation) {
        this.operation = operation;
    }

    private Vector<VariableBinding> getVariableBindings(String[] args, int position) {
        Vector<VariableBinding> v = new Vector<VariableBinding>(args.length - position + 1);
        for (int i = position; i < args.length; i++) {
            String oid = args[i];
            char type = 'i';
            String value = null;
            int equal = oid.indexOf("={");
            if (equal > 0) {
                oid = args[i].substring(0, equal);
                type = args[i].charAt(equal + 2);
                value = args[i].substring(args[i].indexOf('}') + 1);
            } else if (oid.indexOf('-') > 0) {
                StringTokenizer st = new StringTokenizer(oid, "-");
                if (st.countTokens() != 2) {
                    throw new IllegalArgumentException("Illegal OID range specified: '" + oid);
                }
                oid = st.nextToken();
                VariableBinding vbLower = new VariableBinding(new OID(oid));
                v.add(vbLower);
                long last = Long.parseLong(st.nextToken());
                long first = vbLower.getOid().lastUnsigned();
                for (long k = first + 1; k <= last; k++) {
                    OID nextOID =
                            new OID(vbLower.getOid().getValue(), 0, vbLower.getOid().size() - 1);
                    nextOID.appendUnsigned(k);
                    VariableBinding next = new VariableBinding(nextOID);
                    v.add(next);
                }
                continue;
            }
            VariableBinding vb = new VariableBinding(new OID(oid));
            if (value != null) {
                Variable variable;
                switch (type) {
                    case 'i':
                        variable = new Integer32(Integer.parseInt(value));
                        break;
                    case 'u':
                        variable = new UnsignedInteger32(Long.parseLong(value));
                        break;
                    case 's':
                        variable = new OctetString(value);
                        break;
                    case 'x':
                        variable = OctetString.fromString(value, ':', 16);
                        break;
                    case 'd':
                        variable = OctetString.fromString(value, '.', 10);
                        break;
                    case 'b':
                        variable = OctetString.fromString(value, ' ', 2);
                        break;
                    case 'n':
                        variable = new Null();
                        break;
                    case 'o':
                        variable = new OID(value);
                        break;
                    case 't':
                        variable = new TimeTicks(Long.parseLong(value));
                        break;
                    case 'a':
                        variable = new IpAddress(value);
                        break;
                    default:
                        throw new IllegalArgumentException("Variable type " + type
                                + " not supported");
                }
                vb.setVariable(variable);
            }
            v.add(vb);
        }
        return v;
    }

    private Address getAddress(String transportAddress) {
        String transport = "udp";
        int colon = transportAddress.indexOf(':');
        if (colon > 0) {
            transport = transportAddress.substring(0, colon);
            transportAddress = transportAddress.substring(colon + 1);
        }
        // set default port
        if (transportAddress.indexOf('/') < 0) {
            transportAddress += "/161";
        }
        if (transport.equalsIgnoreCase("udp")) {
            return new UdpAddress(transportAddress);
        } else if (transport.equalsIgnoreCase("tcp")) {
            return new TcpAddress(transportAddress);
        }
        throw new IllegalArgumentException("Unknown transport " + transport);
    }

    private void checkOptions() {
        if (operation == SnmpRequest.WALK && pduType != PDU.GETBULK && pduType != PDU.GETNEXT) {
            throw new IllegalArgumentException("Walk operation is not supported for PDU type: "
                    + PDU.getTypeString(pduType));
        } else if (operation == SnmpRequest.WALK && vbs.size() != 1) {
            throw new IllegalArgumentException("There must be exactly one OID supplied for walk operations");
        }
        if (pduType == PDU.V1TRAP && version != SnmpConstants.version1) {
            throw new IllegalArgumentException("V1TRAP PDU type is only available for SNMP version 1");
        }
    }

    private void checkTrapVariables(Vector<VariableBinding> vbs) {
        if (pduType == PDU.INFORM || pduType == PDU.TRAP) {
            if (vbs.size() == 0 || vbs.size() > 1
                    && !vbs.get(0).getOid().equals(SnmpConstants.sysUpTime)) {
                vbs.add(0, new VariableBinding(SnmpConstants.sysUpTime, sysUpTime));
            }
            if (vbs.size() == 1 || vbs.size() > 2
                    && !vbs.get(1).getOid().equals(SnmpConstants.snmpTrapOID)) {
                vbs.add(1, new VariableBinding(SnmpConstants.snmpTrapOID, trapOID));
            }
        }
    }

    private String nextOption(String[] args, int position) {
        if (position + 1 >= args.length) {
            throw new IllegalArgumentException("Missing option value for " + args[position]);
        }
        return args[position + 1];
    }

    private OctetString createOctetString(String s) {
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
    }

    private int parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) {
                String s = nextOption(args, i++);
                if (s.equals("MD5")) {
                    authProtocol = AuthMD5.ID;
                } else if (s.equals("SHA")) {
                    authProtocol = AuthSHA.ID;
                } else {
                    throw new IllegalArgumentException("Authentication protocol unsupported: " + s);
                }
            } else if (args[i].equals("-A")) {
                authPassphrase = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-X") || args[i].equals("-P")) {
                privPassphrase = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-c")) {
                community = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-b")) {
                engineBootCount = Math.max(Integer.parseInt(nextOption(args, i++)), 0);
            } else if (args[i].equals("-d")) {
                String debugOption = nextOption(args, i++);
                LogFactory.getLogFactory()
                          .getRootLogger()
                          .setLogLevel(LogLevel.toLevel(debugOption));
            } else if (args[i].equals("-l")) {
                log.error("-l is not supported");
                // localEngineID = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-e")) {
                log.error("-e is not supported");
                // authoritativeEngineID = createOctetString(nextOption(args,
                // i++));
            } else if (args[i].equals("-E")) {
                contextEngineID = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-n")) {
                contextName = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-m")) {
                maxSizeResponsePDU = Integer.parseInt(nextOption(args, i++));
            } else if (args[i].equals("-r")) {
                retries = Integer.parseInt(nextOption(args, i++));
            } else if (args[i].equals("-t")) {
                timeout = Integer.parseInt(nextOption(args, i++));
            } else if (args[i].equals("-u")) {
                securityName = createOctetString(nextOption(args, i++));
            } else if (args[i].equals("-Cr")) {
                maxRepetitions = Integer.parseInt(nextOption(args, i++));
            } else if (args[i].equals("-Cn")) {
                nonRepeaters = Integer.parseInt(nextOption(args, i++));
            } else if (args[i].equals("-Ce")) {
                v1TrapPDU.setEnterprise(new OID(nextOption(args, i++)));
            } else if (args[i].equals("-Ct")) {
                trapOID = new OID(nextOption(args, i++));
            } else if (args[i].equals("-Cg")) {
                v1TrapPDU.setGenericTrap(Integer.parseInt(nextOption(args, i++)));
            } else if (args[i].equals("-Cs")) {
                v1TrapPDU.setSpecificTrap(Integer.parseInt(nextOption(args, i++)));
            } else if (args[i].equals("-Ca")) {
                v1TrapPDU.setAgentAddress(new IpAddress(nextOption(args, i++)));
            } else if (args[i].equals("-Cu")) {
                String upTime = nextOption(args, i++);
                v1TrapPDU.setTimestamp(Long.parseLong(upTime));
                sysUpTime.setValue(Long.parseLong(upTime));
            } else if (args[i].equals("-Ow")) {
                operation = SnmpRequest.WALK;
            } else if (args[i].equals("-Ol")) {
                operation = SnmpRequest.LISTEN;
            } else if (args[i].equals("-OtCSV")) {
                operation = SnmpRequest.CVS_TABLE;
            } else if (args[i].equals("-OttCSV")) {
                operation = SnmpRequest.TIME_BASED_CVS_TABLE;
            } else if (args[i].equals("-Ot")) {
                operation = SnmpRequest.TABLE;
            } else if (args[i].equals("-Otd")) {
                operation = SnmpRequest.TABLE;
                log.error("-Otd is not supported");
                // useDenseTableOperation = true;
            } else if (args[i].equals("-Cil")) {
                log.error("-Cil is not supported");
                // lowerBoundIndex = new OID(nextOption(args, i++));
            } else if (args[i].equals("-Ciu")) {
                log.error("-Ciu is not supported");
                // upperBoundIndex = new OID(nextOption(args, i++));
            } else if (args[i].equals("-v")) {
                String v = nextOption(args, i++);
                if (v.equals("1")) {
                    version = SnmpConstants.version1;
                } else if (v.equals("2c")) {
                    version = SnmpConstants.version2c;
                } else if (v.equals("3")) {
                    version = SnmpConstants.version3;
                } else {
                    throw new IllegalArgumentException("Version " + v + " not supported");
                }
            } else if (args[i].equals("-x")) {
                String s = nextOption(args, i++);
                if (s.equals("DES")) {
                    privProtocol = PrivDES.ID;
                } else if (s.equals("AES128") || s.equals("AES")) {
                    privProtocol = PrivAES128.ID;
                } else if (s.equals("AES192")) {
                    privProtocol = PrivAES192.ID;
                } else if (s.equals("AES256")) {
                    privProtocol = PrivAES256.ID;
                } else {
                    throw new IllegalArgumentException("Privacy protocol " + s + " not supported");
                }
            } else if (args[i].equals("-p")) {
                String s = nextOption(args, i++);
                pduType = PDU.getTypeFromString(s);
                if (pduType == Integer.MIN_VALUE) {
                    throw new IllegalArgumentException("Unknown PDU type " + s);
                }
            } else if (!args[i].startsWith("-")) {
                return i;
            } else {
                throw new IllegalArgumentException("Unknown option " + args[i]);
            }
        }
        return 0;
    }

    private void addUsmUser(Snmp snmp) {
        snmp.getUSM().addUser(securityName,
                              new UsmUser(securityName,
                                          authProtocol,
                                          authPassphrase,
                                          privProtocol,
                                          privPassphrase));
    }

    private Snmp createSnmpSession() throws IOException {
        AbstractTransportMapping transport;
        if (address instanceof TcpAddress) {
            transport = new DefaultTcpTransportMapping();
        } else {
            transport = new DefaultUdpTransportMapping();
        }
        // Could save some CPU cycles:
        // transport.setAsyncMsgProcessingSupported(false);
        Snmp snmp = new Snmp(transport);

        if (version == SnmpConstants.version3) {
            USM usm =
                    new USM(SecurityProtocols.getInstance(),
                            new OctetString(MPv3.createLocalEngineID()),
                            engineBootCount);
            SecurityModels.getInstance().addSecurityModel(usm);
            addUsmUser(snmp);
        }
        return snmp;
    }

    public PDU createPDU(Target target) {
        PDU request;
        if (target.getVersion() == SnmpConstants.version3) {
            request = new ScopedPDU();
            ScopedPDU scopedPDU = (ScopedPDU) request;
            if (contextEngineID != null) {
                scopedPDU.setContextEngineID(contextEngineID);
            }
            if (contextName != null) {
                scopedPDU.setContextName(contextName);
            }
        } else {
            if (pduType == PDU.V1TRAP) {
                request = v1TrapPDU;
            } else {
                request = new PDU();
            }
        }
        request.setType(pduType);
        return request;
    }

    private Target createTarget() {
        if (version == SnmpConstants.version3) {
            UserTarget target = new UserTarget();
            if (authPassphrase != null) {
                if (privPassphrase != null) {
                    target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                } else {
                    target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
                }
            } else {
                target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            }
            target.setSecurityName(securityName);
            return target;
        } else {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(community);
            return target;
        }
    }

    public PDU send() throws IOException {
        Snmp snmp = createSnmpSession();
        this.target = createTarget();
        target.setVersion(version);
        target.setAddress(address);
        target.setRetries(retries);
        target.setTimeout(timeout);
        target.setMaxSizeRequestPDU(maxSizeResponsePDU);
        snmp.listen();

        PDU request = createPDU(target);
        if (request.getType() == PDU.GETBULK) {
            request.setMaxRepetitions(maxRepetitions);
            request.setNonRepeaters(nonRepeaters);
        }
        for (int i = 0; i < vbs.size(); i++) {
            request.add(vbs.get(i));
        }

        PDU response = null;
        if (operation == SnmpRequest.WALK) {
            WalkListener body = new WalkListener() {
                public void walkEvent(VariableBinding[] bindings) {
                    for (VariableBinding binding : bindings) {
                        log.info(binding.toString());
                    }
                }

                public void walkCompletion(boolean success) {
                }
            };
            walk(snmp, request, target, body);
            return null;
        } else {
            ResponseEvent responseEvent;
            long startTime = System.currentTimeMillis();
            responseEvent = snmp.send(request, target);
            if (responseEvent != null) {
                response = responseEvent.getResponse();
                log.info("Received response after " + (System.currentTimeMillis() - startTime)
                        + " millis");
            }
        }
        snmp.close();
        return response;
    }
    
    public void send(WalkListener body) throws IOException {
        Snmp snmp = createSnmpSession();
        this.target = createTarget();
        target.setVersion(version);
        target.setAddress(address);
        target.setRetries(retries);
        target.setTimeout(timeout);
        target.setMaxSizeRequestPDU(maxSizeResponsePDU);
        snmp.listen();

        PDU request = createPDU(target);
        if (request.getType() == PDU.GETBULK) {
            request.setMaxRepetitions(maxRepetitions);
            request.setNonRepeaters(nonRepeaters);
        }
        for (int i = 0; i < vbs.size(); i++) {
            request.add(vbs.get(i));
        }

        walk(snmp, request, target, body);
    }

    private PDU walk(Snmp snmp, PDU request, Target target, final WalkListener body) {
        request.setNonRepeaters(0);
        OID rootOID = request.get(0).getOid();
        PDU response = null;
        final WalkCounts counts = new WalkCounts();
        final long startTime = System.currentTimeMillis();
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        treeUtils.getSubtree(target, rootOID, null, new TreeListener() {
            public boolean next(TreeEvent e) {
                counts.requests++;
                VariableBinding[] variableBindings = e.getVariableBindings();
                if (variableBindings != null) {
                    counts.objects += variableBindings.length;
                    body.walkEvent(variableBindings);
                }
                return true;
            }

            public void finished(TreeEvent e) {
                if (log.isInfoEnabled()) {
                    log.info("Total walk time:        " + (System.currentTimeMillis() - startTime)
                            + " milliseconds");
                    log.info("Total requests sent:    " + counts.requests);
                    log.info("Total objects received: " + counts.objects);
                }
                if (e.isError()) {
                    log.error("The following error occurred during walk:");
                    log.error(e.getErrorMessage());
                }
                body.walkCompletion(!e.isError());
            }
        });
        return response;
    }

    private static final class WalkCounts {
        public int requests;
        public int objects;
    }
}
