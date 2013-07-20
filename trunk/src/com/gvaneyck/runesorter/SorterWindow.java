package com.gvaneyck.runesorter;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.gvaneyck.util.Callback;
import com.gvaneyck.util.Tuple;

public class SorterWindow extends JFrame {
    private static final long serialVersionUID = -4909493574753556479L;

    private final JButton btnSort = new JButton("Sort Alphabetically");
    private final JButton btnMoveUp = new JButton("Move Up");
    private final JButton btnMoveDown = new JButton("Move Down");

    private final JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);

    private final JLabel lblRunePages = new JLabel("Rune Pages");
    private final RunePageListModel lstRuneModel = new RunePageListModel();
    private final JList lstRunePages = new JList(lstRuneModel);
    private final JScrollPane lstRuneScroll = new JScrollPane(lstRunePages);

    private final JLabel lblMasteryPages = new JLabel("Mastery Pages");
    private final MasteryPageListModel lstMasteryModel = new MasteryPageListModel();
    private final JList lstMasteryPages = new JList(lstMasteryModel);
    private final JScrollPane lstMasteryScroll = new JScrollPane(lstMasteryPages);
    
    private final JLabel lblSide = new JLabel("");

    private int lastSelectedPage = -1;
    private int lastSelectedMasteries = -1;
    private boolean changingSelection = false;

    public static int width = 550;
    public static int height = 320;
    
    // Callbacks
    public Callback runeSorterListener;
    public Callback masterySorterListener;
    public Callback runeSwapListener;
    public Callback masterySwapListener;
    public Callback runeSelectListener;
    public Callback masterySelectListener;

    public SorterWindow() {
        setTitle("Rune/Mastery Page Manager");
        
        // GUI settings
        lstRunePages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lblSide.setVerticalAlignment(SwingConstants.TOP);

        // Initially grey out buttons
        btnSort.setEnabled(false);
        btnMoveUp.setEnabled(false);
        btnMoveDown.setEnabled(false);

        // Add the items
        Container pane = getContentPane();
        pane.setLayout(null);

        pane.add(btnSort);
        pane.add(btnMoveUp);
        pane.add(btnMoveDown);

        pane.add(sep);

        pane.add(lblRunePages);
        pane.add(lstRuneScroll);
        pane.add(lblMasteryPages);
        pane.add(lstMasteryScroll);
        
        pane.add(lblSide);

        // Listeners
        btnSort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedPage != -1)
                    if (runeSorterListener != null)
                        runeSorterListener.callback(null);

                if (lastSelectedMasteries != -1)
                    if (masterySorterListener != null)
                        masterySorterListener.callback(null);
            }
        });

        btnMoveUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedPage != -1 && lastSelectedPage != 0) {
                    if (runeSwapListener != null)
                        runeSwapListener.callback(new Tuple(lastSelectedPage, lastSelectedPage - 1));

                    lastSelectedPage--;
                    lstRunePages.setSelectedIndex(lastSelectedPage);
                }
                if (lastSelectedMasteries != -1 && lastSelectedMasteries != 0) {
                    if (masterySwapListener != null)
                        masterySwapListener.callback(new Tuple(lastSelectedPage, lastSelectedPage - 1));

                    lastSelectedMasteries--;
                    lstMasteryPages.setSelectedIndex(lastSelectedMasteries);
                }
            }
        });

        btnMoveDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedPage != -1 && lastSelectedPage != lstRunePages.getModel().getSize() - 1) {
                    if (runeSwapListener != null)
                        runeSwapListener.callback(new Tuple(lastSelectedPage, lastSelectedPage + 1));

                    lastSelectedPage++;
                    lstRunePages.setSelectedIndex(lastSelectedPage);
                }
                if (lastSelectedMasteries != -1 && lastSelectedMasteries != lstMasteryPages.getModel().getSize() - 1) {
                    if (masterySwapListener != null)
                        masterySwapListener.callback(new Tuple(lastSelectedPage, lastSelectedPage + 1));

                    lastSelectedMasteries++;
                    lstMasteryPages.setSelectedIndex(lastSelectedMasteries);
                }
            }
        });

        lstRunePages.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (changingSelection)
                    return;

                changingSelection = true;
                lastSelectedPage = lstRunePages.getSelectedIndex();
                lstMasteryPages.clearSelection();
                lastSelectedMasteries = -1;
                changingSelection = false;
                
                if (runeSelectListener != null)
                    runeSelectListener.callback(lastSelectedPage);
            }
        });

        lstMasteryPages.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (changingSelection)
                    return;

                changingSelection = true;
                lastSelectedMasteries = lstMasteryPages.getSelectedIndex();
                lastSelectedPage = -1;
                lstRunePages.clearSelection();
                changingSelection = false;

                if (masterySelectListener != null)
                    masterySelectListener.callback(lastSelectedPage);
            }
        });

        pane.addHierarchyBoundsListener(new HierarchyBoundsListener() {
            public void ancestorMoved(HierarchyEvent e) {}

            public void ancestorResized(HierarchyEvent e) {
                Dimension d = getSize();
                width = d.width;
                height = d.height;
                doMyLayout();
            }
        });

        // Window settings
        setSize(width, height);
        setMinimumSize(new Dimension(width, height));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    public void enableButtons() {
        btnSort.setEnabled(true);
        btnMoveUp.setEnabled(true);
        btnMoveDown.setEnabled(true);
    }
    
    public void disableButtons() {
        btnSort.setEnabled(false);
        btnMoveUp.setEnabled(false);
        btnMoveDown.setEnabled(false);
    }
    
    public void updateRunePages(List<RunePage> pages) {
        lstRuneModel.clear();
        for (RunePage page : pages)
            lstRuneModel.add(page);
        lstRuneModel.update();
    }
    
    public void updateMasteryPages(List<MasteryPage> pages) {
        lstMasteryModel.clear();
        for (MasteryPage page : pages)
            lstMasteryModel.add(page);
        lstMasteryModel.update();
    }
    
    public void setInfo1(String text) {
        lblSide.setText(text);
    }
    
    private void doMyLayout() {
        Insets i = getInsets();
        int twidth = width - i.left - i.right - 300;
        int theight = height - i.top - i.bottom;

        btnSort.setBounds(5, 5, twidth - 10, 24);
        btnMoveUp.setBounds(5, 34, twidth / 2 - 10, 24);
        btnMoveDown.setBounds(5 + twidth / 2, 34, twidth / 2 - 10, 24);

        sep.setBounds(5, 63, twidth - 10, 5);

        int scrollHeight = theight / 2 - 50;
        lblRunePages.setBounds(5, 65, twidth - 9, 15);
        lstRuneScroll.setBounds(5, 80, twidth - 9, scrollHeight);
        lblMasteryPages.setBounds(5, 80 + scrollHeight, twidth - 9, 15);
        lstMasteryScroll.setBounds(5, 95 + scrollHeight, twidth - 9, scrollHeight);
        
        lblSide.setBounds(twidth + 5, 5, 290, theight - 10);
    }
}