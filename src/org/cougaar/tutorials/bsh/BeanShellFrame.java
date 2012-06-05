/* *************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2006 by BBNT Solutions, LLC.
 * =========================================================================
 * </rrl>
 *
 * $Id: BeanShellFrame.java,v 1.1 2007-06-20 19:13:13 jzinky Exp $
 *
 * ************************************************************************/

package org.cougaar.tutorials.bsh;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;

/**
 * A frame containing a Bean Shell console. Unfortunately, I can't figure out
 * how to gracefully stop the interpreter. That means it might be best to hold
 * on to the frame, redisplaying it as needed.
 * 
 * @version $Revision: 1.1 $ on $Date: 2007-06-20 19:13:13 $
 */
public class BeanShellFrame
      extends JFrame {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /** The Bean Shell console window. */
   private JConsole console;

   /** The Bean Shell interpreter. */
   private Interpreter interpreter;

   /**
    * Constructs a new Bean Shell Frame. The frame consists of a bean shell
    * console and a close button. The default size is 500 x 300.
    */
   public BeanShellFrame() {
      this("Bean Shell");
   }

   /**
    * Constructs a new Bean Shell Frame. The frame consists of a bean shell
    * console and a close button. The default size is 500 x 300.
    */
   public BeanShellFrame(String title) {
      super(title);
      console = new JConsole();
      interpreter = new Interpreter(console);
      init(interpreter);

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(console, BorderLayout.CENTER);
      JPanel buttonPanel = makeButtonPanel();
      if (buttonPanel != null) {
         getContentPane().add(buttonPanel, BorderLayout.SOUTH);
      }
      setSize(700, 600);
   }

   /**
    * Performs any desired initializations on the Bean Shell Interpreter. This
    * method can be overridden in extending classes to perform additional
    * initializations on the interpreter.
    * 
    * @param i the bean shell interpreter
    */
   protected void init(Interpreter i) {
      try {
         i.set("bsh.system.shutdownOnExit", false);
         i.eval("show();");
      } catch (EvalError e) {
         // Do nothing, our inits didn't work out.
         System.out.println("BeanShellFrame.init failure: ");
         e.printStackTrace();
      }
   }

   /**
    * Creates the button panel on the frame. The default is a close button.
    * Extending classes can create a more compilcated button panel if desired by
    * overriding this method.
    */
   protected JPanel makeButtonPanel() {
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
         }
      });

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(closeButton);
      return buttonPanel;
   }

   /**
    * Import a class or package into the interpreter
    * 
    * @param path the fully qualified class or package name
    **/
   public void interpreterImport(String path) {
      try {
         interpreter.eval("import " + path);
      } catch (EvalError e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets the bean shell variable <code>var</code> to <code>obj</code> in the
    * bean shell interpreter of this frame.
    * 
    * @param var the bean shell variable to set
    * @param obj the value of the beans shell variable <code>var</code>
    */
   public void set(String var, Object obj) {
      try {
         interpreter.set(var, obj);
      } catch (EvalError e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Runs this interpreter in the current thread. I don't think this method
    * will ever return because of limitations in the bean shell interpreter API.
    */
   public void run() {
      interpreter.run();
   }

   /**
    * Runs this interpreter in its own thread.
    */
   public void runInThread() {
      new Thread(interpreter).start();
   }

   public static void main(String[] argv) {
      BeanShellFrame bsf = new BeanShellFrame();
      bsf.setTitle("Bean Shell");
      bsf.pack();
      bsf.setVisible(true);
      bsf.run();
   }
}
