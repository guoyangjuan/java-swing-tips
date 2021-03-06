package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                //???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new CheckBoxNodeRenderer());
                setCellEditor(new CheckBoxNodeEditor(this));
            }
        };
        boolean b = true;
        TreeModel model = tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            Object o = node.getUserObject();
            if (o instanceof String) {
                b ^= true;
                node.setUserObject(new CheckBoxNode(o.toString(), b));
            }
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        //tree.setCellRenderer(new CheckBoxNodeRenderer());
        //tree.setCellEditor(new CheckBoxNodeEditor(tree));
        tree.setEditable(true);

        add(makeTitledPanel("JCheckBoxes as JTree Leaf Nodes", tree));
        setPreferredSize(new Dimension(320, 240));
    }
//     protected static TreeModel getDefaultTreeModel() {
//         DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
//         DefaultMutableTreeNode parent;
//
//         parent = new DefaultMutableTreeNode("colors");
//         root.add(parent);
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("blue",   false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("violet", false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("red",    false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("yellow", false)));
//
//         parent = new DefaultMutableTreeNode("sports");
//         root.add(parent);
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("basketball", true)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("soccer",     true)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("football",   true)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("hockey",     true)));
//
//         parent = new DefaultMutableTreeNode("food");
//         root.add(parent);
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("hot dogs", false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("pizza",    false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("ravioli",  false)));
//         parent.add(new DefaultMutableTreeNode(new CheckBoxNode("bananas",  false)));
//         return new DefaultTreeModel(root);
//     }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class CheckBoxNodeRenderer extends JCheckBox implements TreeCellRenderer {
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (leaf && value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            this.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                this.setText(node.text);
                this.setSelected(node.selected);
            }
            return this;
        }
        return renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
    @Override public void updateUI() {
        super.updateUI();
        setName("Tree.cellRenderer");
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if (System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }
}

class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
    private final JTree tree;
    public CheckBoxNodeEditor(JTree tree) {
        super();
        this.tree = tree;
        setOpaque(false);
        addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (leaf && value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                this.setSelected(((CheckBoxNode) userObject).selected);
            } else {
                this.setSelected(false);
            }
            this.setText(value.toString());
        }
        return this;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(getText(), isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());
            Object o = path.getLastPathComponent();
            if (o instanceof TreeNode) {
                return ((TreeNode) o).isLeaf();
            }
        }
        return false;
    }
    @Override public void updateUI() {
        super.updateUI();
        setName("Tree.cellEditor");
    }
    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}

class CheckBoxNode {
    public final String text;
    public final boolean selected;
    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
//     public boolean isSelected() {
//         return selected;
//     }
//     public void setSelected(boolean newValue) {
//         selected = newValue;
//     }
//     public String getText() {
//         return text;
//     }
//     public void setText(String newValue) {
//         text = newValue;
//     }
    @Override public String toString() {
        return text;
    }
}

//TEST:
// class CheckBoxNodeRenderer extends DefaultTreeCellRenderer {
//     private final JCheckBox leafRenderer = new JCheckBox();
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         this.tree = tree;
//         //String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
//         if (leaf && value instanceof DefaultMutableTreeNode) {
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 leafRenderer.setEnabled(tree.isEnabled());
//                 leafRenderer.setFont(getFont());
//                 leafRenderer.setOpaque(false);
// //                 if (selected) {
// //                     leafRenderer.setForeground(getTextSelectionColor());
// //                     leafRenderer.setBackground(getBackgroundSelectionColor());
// //                 } else {
// //                     leafRenderer.setForeground(getTextNonSelectionColor());
// //                     leafRenderer.setBackground(getBackgroundNonSelectionColor());
// //                 }
//                 CheckBoxNode node = (CheckBoxNode) userObject;
//                 leafRenderer.setText(node.text); //leafRenderer.setText(stringValue);
//                 leafRenderer.setSelected(node.selected);
//                 return leafRenderer;
//             }
//         }
//         JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
// //         if (selected) {
// //             c.setForeground(getTextSelectionColor());
// //             c.setBackground(getBackgroundSelectionColor());
// //         } else {
// //             c.setForeground(getTextNonSelectionColor());
// //             c.setBackground(getBackgroundNonSelectionColor());
// //         }
//         //c.setOpaque(false);
//         return c;
//     }
//     private JTree tree = null;
//     @Override public void updateUI() {
//         if (Objects.nonNull(tree)) {
//             tree.setCellRenderer(null);
//             super.updateUI();
//             TreeCellRenderer r = tree.getCellRenderer();
//
//         }
//         if (Objects.nonNull(leafRenderer)) { leafRenderer.updateUI(); }
//
//         //setLeafIcon(getDefaultLeafIcon());
//         setOpenIcon(getDefaultOpenIcon());
//         setClosedIcon(getDefaultClosedIcon());
//
// //         if (getTextSelectionColor() instanceof UIResource) {
// //             setTextSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
// //         }
// //         if (getTextNonSelectionColor() instanceof UIResource) {
// //             setTextNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textForeground"));
// //         }
// //         if (getBackgroundSelectionColor() instanceof UIResource) {
// //             setBackgroundSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
// //         }
// //         if (getBackgroundNonSelectionColor() instanceof UIResource) {
// //             setBackgroundNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textBackground"));
// //         }
// //         if (getBorderSelectionColor() instanceof UIResource) {
// //             setBorderSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
// //         }
// // //         drawsFocusBorderAroundIcon = DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
// // //         drawDashedFocusIndicator = DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
// // //         fillBackground = DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
// //         Insets margins = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
// //         if (Objects.nonNull(margins)) {
// //             setBorder(new EmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
// //             //if (Objects.nonNull(leafRenderer)) { leafRenderer.setBorder(getBorder()); }
// //         }
//         setName("Tree.cellRenderer");
//         //if (Objects.nonNull(tree)) tree.revalidate();
//     }
// }
