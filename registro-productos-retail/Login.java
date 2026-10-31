import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;

    public Login() {
        setTitle("Acceso Restringido - Gestión de Productos");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ingrese Credenciales de Admin"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; txtUsuario = new JTextField(12); panel.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; txtContrasena = new JPasswordField(12); panel.add(txtContrasena, gbc);

        btnIngresar = new JButton("Ingresar al CRUD");
        btnIngresar.setBackground(new Color(41, 128, 185));
        btnIngresar.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        add(panel, BorderLayout.CENTER);

        // Evento de login (clic o tecla Enter)
        btnIngresar.addActionListener(e -> validarLogin());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void validarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtContrasena.getPassword()).trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba el usuario y la contraseña.", "Faltan datos", JOptionPane.WARNING_MESSAGE);
            (usuario.isEmpty() ? txtUsuario : txtContrasena).requestFocusInWindow();
            return;
        }

        // Credenciales fijas de ejemplo (puedes cambiarlas o validarlas con BD si prefieres)
        if (usuario.equals("admin") && password.equals("1234")) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido, Administrador!", "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
            // Abre la ventana de gestión de productos (CRUD)
            new RegistroProductosRetail().setVisible(true);
            this.dispose(); // Cierra la ventana de login
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
            txtContrasena.setText("");
            txtContrasena.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}