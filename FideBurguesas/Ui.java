import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class Ui {
    private Ui() {}
    static void error(Component p, Exception e) {
        JOptionPane.showMessageDialog(p, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    static JPanel fila(String t, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JLabel l = new JLabel(t); l.setPreferredSize(new Dimension(90, 24));
        p.add(l, BorderLayout.WEST); p.add(c);
        return p;
    }
    static DefaultTableModel modelo(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
    static String money(double d) { return String.format("₡%,.0f", d); }
}
