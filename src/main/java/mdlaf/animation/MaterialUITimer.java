/**
 * MIT License
 * <p>
 * Copyright (c) 2018-2020 atharva washimkar, Vincenzo Palazzo vincenzopalazzo1996@gmail.com
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mdlaf.animation;

import mdlaf.components.button.MaterialButtonUI;
import mdlaf.utils.MaterialLogger;


import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author https://github.com/vincenzopalazzo
 * @author https://github.com/atarw
 */
public class MaterialUITimer implements ActionListener, MaterialMouseHover {

    private static final Class LOG_TAG = MaterialUITimer.class;


    private Color from, to;
    private boolean forward;
    private int alpha, steps;
    private int[] forwardDeltas, backwardDeltas;
    private JComponent component;
    private Timer timer;
    private WrapperInformationsButton wrapperInformationsButton;

    protected MaterialUITimer(JComponent component, Color to, int steps, int interval) {
        //the code  !component.isEnabled() is commented because if the button born disabled
        //the mouse hover will never install
        if (component == null /*|| !component.isEnabled()*/) {
            return;
        }
        if (component.getCursor().getType() == Cursor.WAIT_CURSOR) {
            //TODO this is an refactoring
            return;
        }
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            if(button.getUI() instanceof MaterialButtonUI){
                MaterialButtonUI materialButtonUI = (MaterialButtonUI) button.getUI();
                if (button.isDefaultButton()) {
                    this.from = UIManager.getColor("Button[Default].background");
                    materialButtonUI.setColorMouseHoverDefaultButton(to);
                } else {
                    this.from = component.getBackground();
                    materialButtonUI.setColorMouseHoverNormalButton(to);
                }
            }
            //wrapperInformationsButton = new WrapperInformationsButton(button);
        } else {
            this.from = component.getBackground();
        }

        MaterialLogger.getInstance().debug(LOG_TAG, "Mouse listener installed on component: " + LOG_TAG.getCanonicalName());
        this.to = to;

        this.forwardDeltas = new int[4];
        this.backwardDeltas = new int[4];

        forwardDeltas[0] = (from.getRed() - to.getRed()) / steps;
        forwardDeltas[1] = (from.getGreen() - to.getGreen()) / steps;
        forwardDeltas[2] = (from.getBlue() - to.getBlue()) / steps;
        forwardDeltas[3] = (from.getAlpha() - to.getAlpha()) / steps;

        backwardDeltas[0] = (to.getRed() - from.getRed()) / steps;
        backwardDeltas[1] = (to.getGreen() - from.getGreen()) / steps;
        backwardDeltas[2] = (to.getBlue() - from.getBlue()) / steps;
        backwardDeltas[3] = (to.getAlpha() - from.getAlpha()) / steps;

        this.steps = steps;

        this.component = component;
        //this.component.addMouseListener(this);
        timer = new Timer(interval, this);
        component.setBackground(from);
    }

    private Color nextColor() {
        int rValue = from.getRed() - alpha * forwardDeltas[0];
        int gValue = from.getGreen() - alpha * forwardDeltas[1];
        int bValue = from.getBlue() - alpha * forwardDeltas[2];
        int aValue = from.getAlpha() - alpha * forwardDeltas[3];

        return new Color(rValue, gValue, bValue, aValue);
    }

    private Color previousColor() {
        int rValue = to.getRed() - (steps - alpha) * backwardDeltas[0];
        int gValue = to.getGreen() - (steps - alpha) * backwardDeltas[1];
        int bValue = to.getBlue() - (steps - alpha) * backwardDeltas[2];
        int aValue = to.getAlpha() - (steps - alpha) * backwardDeltas[3];

        return new Color(rValue, gValue, bValue, aValue);
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (!me.getComponent().isEnabled()) {
            return;
        }
        alpha = steps - 1;
        forward = false;
        if (timer.isRunning()) {
            timer.stop();
        }
        timer.start();

        alpha = 0;
        forward = true;
        timer.start();
    }

    @Override
    public void mouseExited(MouseEvent me) {
        if (!me.getComponent().isEnabled()) {
            return;
        }
        if (timer.isRunning()) {
            timer.stop();
        }
        alpha = steps - 1;
        forward = false;
        timer.start();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        if (!me.getComponent().isEnabled()) {
            return;
        }
        alpha = 0;
        forward = true;
        if (timer.isRunning()) {
            timer.stop();
        }
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        MaterialLogger.getInstance().debug(LOG_TAG, "Timer class, method actionPerformed called for component: " + component.getName());
        if (!component.isEnabled()) {
            //if (timer.isRunning()) {
                /*if ((component instanceof JButton) &&
                        wrapperInformationsButton != null) {
                    System.out.println("component button set color");
                    JButton buttonComponent = (JButton) component;
                    wrapperInformationsButton.setOriginValues(buttonComponent);
                }*/
              //  timer.stop();
            //}
            this.stopTimer();
            return;
        }
        if (forward) {
            component.setBackground(nextColor());
            ++alpha;
        } else {
            component.setBackground(previousColor());
            --alpha;
        }
        if (alpha == steps + 1 || alpha == -1) {
            if (timer.isRunning()) {
                timer.stop();
            }
        }
        //For some color the algorithm not work well, so
        //when the alpha is -1 the mouse is exist from button
        if (alpha == -1) {
            //Mouse exit
            this.component.setBackground(this.from);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //do nothing this is util only implements interface MouseMotions
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //do nothing this is util only implements interface MouseMotions
        this.stopTimer();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        //do nothing
        this.stopTimer();
/*
        if ((component instanceof JButton) &&
                wrapperInformationsButton != null) {
            JButton buttonComponent = (JButton) component;
            wrapperInformationsButton.setOriginValues(buttonComponent);
        }*/
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //do nothing
        this.stopTimer();
/*
        if ((component instanceof JButton) &&
                wrapperInformationsButton != null) {
            JButton buttonComponent = (JButton) component;
            wrapperInformationsButton.setOriginValues(buttonComponent);
        }*/
    }

    protected void stopTimer(){
        if (component != null && !component.isEnabled()) {
            if (timer != null && timer.isRunning()) {
            /*    if ((component instanceof JButton) &&
                        wrapperInformationsButton != null) {
                    JButton buttonComponent = (JButton) component;
                    wrapperInformationsButton.setOriginValues(buttonComponent);
                }*/
                timer.stop();
            }
        }
    }


    @Deprecated
    private class WrapperInformationsButton {

        private Color background;
        private Color foreground;
        private Color defaultBackground;
        private Color defaultForeground;
        private Color disabledForeground;
        private Color disabledBackground;

        public WrapperInformationsButton(JButton button) {
            if (!(button.getUI() instanceof MaterialButtonUI)) {
                throw new RuntimeException("UI button's not instance of MaterialButtonUI");
            }
            MaterialButtonUI materialButtonUI = (MaterialButtonUI) button.getUI();
            this.background = materialButtonUI.getBackground();
            this.foreground = materialButtonUI.getForeground();
            this.disabledBackground = materialButtonUI.getDisabledBackground();
            this.disabledForeground = materialButtonUI.getDisabledForeground();
            if (materialButtonUI.isDefaultButton()) {
                this.defaultBackground = materialButtonUI.getDefaultBackground();
                this.defaultForeground = materialButtonUI.getDefaultForeground();
            }

        }

        public synchronized void setOriginValues(JButton button) {
            if (button == null || !(button.getUI() instanceof MaterialButtonUI)) {
                String messsage;
                if (button == null) {
                    messsage = "Button component null";
                } else {
                    messsage = "UI button's not instance of MaterialButtonUI";
                }
                throw new RuntimeException(messsage);
            }
            MaterialButtonUI materialButtonUI = (MaterialButtonUI) button.getUI();
            materialButtonUI.setBackground(this.background);
            materialButtonUI.setForeground(this.foreground);
            materialButtonUI.setDisabledBackground(this.disabledBackground);
            materialButtonUI.setDisabledForeground(this.disabledForeground);
            if (materialButtonUI.isDefaultButton()) {
                if (this.defaultBackground == null || this.defaultForeground == null) {
                    throw new RuntimeException("Value defaultBackground or/and defaultForeground is/are null");
                }
                materialButtonUI.setDefaultBackground(this.defaultBackground);
                materialButtonUI.setDefaultForeground(this.defaultForeground);
            }
            button.repaint();
        }
    }
}
