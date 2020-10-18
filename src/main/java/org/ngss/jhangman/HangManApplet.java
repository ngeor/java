/*
 * HangManApplet.java
 *
 * Created on August 5, 2004, 6:00 PM
 */

package org.ngss.jhangman;

import javax.swing.JApplet;

/**
 * @author ngeor
 */
public class HangManApplet extends JApplet {
    private HangManPanel hmp = new HangManPanel();

    public void setLastAnswer(String s) {
        // not used
    }

    public String getAnswersBuffer() {
        return "";
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public float getValue() {
        return (hmp.isSolved()) ? 100 : 0;
    }

    /**
     * Initializes the applet HangManApplet.
     */
    public void init() {
        initComponents();


        hmp.startGame(getParameter("question"), getParameter("answer"), getParameter("lang"));

        getContentPane().add(hmp, java.awt.BorderLayout.CENTER);
    }

    /**
     * This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
