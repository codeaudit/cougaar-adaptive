/* 
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.qos.qrs.ospf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 *
 */
public class IndirectBlackOspfDataFeed extends SimpleQueueingDataFeed implements Constants {
    private static final String USER_NAME_ARG = "--user=";
    private static final String IDENTITY_FILE_ARG = "--identity-file=";
    private static final String HOST_ARG = "--host=";
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    
    private final Logger log;
    private long pollPeriodMillis = 2000;
    private String user;
    private String host;
    private File identityFile;
    private String[] command;
    private PrintStream printer;
    private BufferedReader inReader;

    public IndirectBlackOspfDataFeed(String[] args) {
        log = Logging.getLogger(getClass());
        for (String arg : args) {
            if (arg.startsWith(USER_NAME_ARG)) {
                user = arg.substring(USER_NAME_ARG.length());
            } else if (arg.startsWith(IDENTITY_FILE_ARG)) {
                identityFile = new File(arg.substring(IDENTITY_FILE_ARG.length()));
            } else if (arg.startsWith(HOST_ARG)) {
                host = arg.substring(HOST_ARG.length());
            } else if (arg.startsWith(POLL_PERIOD_ARG)) {
                pollPeriodMillis = Long.parseLong(arg.substring(POLL_PERIOD_ARG.length()));
            }
        }
        if (host == null || user == null || identityFile == null) {
            log.error("Missing arguments");
            return;
        }
        if (!identityFile.exists()) {
            log.error("Identity file " +identityFile+ " does not exist");
            return;
        }
        command = new String[] {
              "ssh",
              "-o",
              "BatchMode=yes",
              "-i",
              identityFile.getAbsolutePath(),
              "-l",
              user,
              host,
              "telnet",
              "10.10.0.120",
              "2604"
        };
        
        RSSUtils.schedule(new Connector(), 0); 
    }
    
    private void parseLine(String line, Map<SiteAddress, Integer> map) {
        String[] fields = line.split("\\s+");
        if (fields.length != 9) {
            log.error("Expected 9 fields, found " +fields.length+ " in " + line);
            return;
        }
        log.shout(fields[0] + " = " + fields[3]);
    }
    
    private String readLineOrPrompt(String prompt) throws IOException {
        char[] buf = new char[prompt.length()];
        inReader.read(buf);
        String bufString = new String(buf);
        if (bufString.equals(prompt)) {
            return null;
        }
        String remaning = inReader.readLine();
        if (remaning == null) {
            return bufString;
        } else {
            return bufString + remaning;
        }
    }
    
    private Map<SiteAddress, Integer> execCommand(String promptString) throws IOException {
        printer.println("show ip rospf lower-layer-neighbor eth1");
        printer.flush();
        String line;
        while ((line=inReader.readLine()) != null) {
            if (line.startsWith("NbrID")) {
                break;
            } else if (line.startsWith("%")) {
                log.error(line);
                return null;
            }
        }
        Map<SiteAddress, Integer> map = new HashMap<SiteAddress, Integer>();
        while ((line=readLineOrPrompt(promptString)) != null) {
            try {
                parseLine(line, map);
            } catch (NumberFormatException e) {
                log.error("Error parsing \"" +line+ "\"", e);
            }
        }
        return map;
    }
    
    private final class Connector implements Runnable {
        String password = "zebra";
        public void run() {
            try {
                Process p = Runtime.getRuntime().exec(command);
                OutputStream stdin = p.getOutputStream();
                printer = new PrintStream(stdin);
                InputStream stdout = p.getInputStream();
                inReader = new BufferedReader(new InputStreamReader(stdout));
                String line;
                while ((line=inReader.readLine()) != null) {
                    if (line.startsWith("User Access Verification")) {
                        inReader.readLine(); // skip blank line
                        char[] prompt = new char[10];
                        inReader.read(prompt);
                        printer.println(password);
                        printer.flush();
                        inReader.readLine(); // skip blank line
                        break;
                    }
                }
                char[] prompt = new char[8];
                inReader.read(prompt);
                String promptString = new String(prompt);
                RSSUtils.schedule(new Poller(promptString), 0, pollPeriodMillis); 
            } catch (IOException e) {
                log.error("Failed to establish connection", e);
            }
        }
    }
    
    /**
     * Run netstat remotely and publish on this data feed as
     * inter-site capacity DataValue.
     * 
     */
    private final class Poller implements Runnable {
        private final String promptString;
        
        Poller(String promptString) {
            this.promptString = promptString;
        }
        
        public void run() {
            try {
                Map<SiteAddress, Integer> map = execCommand(promptString);
                if (map == null) {
                    return;
                }
                for (Map.Entry<SiteAddress, Integer> entry : map.entrySet()) {
                    log.info(entry.getKey() + " = " + entry.getValue());
                }
            } catch (IOException e) {
                log.error("Netstat request failed", e);
            }
        }
    }

}
