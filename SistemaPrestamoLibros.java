import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SistemaPrestamoLibros extends JFrame {

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtRut;
    private JComboBox<String> cbLibro;
    private JComboBox<String> cbTipoUsuario;
    private JTextField txtCantidad;
    private JButton btnRegistrar;

    public SistemaPrestamoLibros() {
        // Configuración de la ventana principal
        setTitle("Sistema de Préstamo de Libros");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Nombre del usuario
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nombre del usuario:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        add(txtNombre, gbc);

        // 2. RUT del usuario
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("RUT del usuario:"), gbc);
        gbc.gridx = 1;
        txtRut = new JTextField(20);
        add(txtRut, gbc);

        // 3. Libro seleccionado
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Libro seleccionado:"), gbc);
        gbc.gridx = 1;
        String[] libros = {"Cien años de soledad", "Don Quijote de la Mancha", "El principito", "Rayuela", "Ficciones"};
        cbLibro = new JComboBox<>(libros);
        add(cbLibro, gbc);

        // 4. Tipo de usuario
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Tipo de usuario:"), gbc);
        gbc.gridx = 1;
        String[] tiposUsuario = {"Estudiante", "Docente", "Administrativo"};
        cbTipoUsuario = new JComboBox<>(tiposUsuario);
        add(cbTipoUsuario, gbc);

        // 5. Cantidad de libros
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Cantidad de libros:"), gbc);
        gbc.gridx = 1;
        txtCantidad = new JTextField(20);
        add(txtCantidad, gbc);

        // Botón Registrar
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        btnRegistrar = new JButton("Registrar Préstamo");
        add(btnRegistrar, gbc);

        // Evento del botón
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarYRegistrar();
            }
        });
    }

    private void validarYRegistrar() {
        String nombre = txtNombre.getText().trim();
        String rut = txtRut.getText().trim();
        String libro = (String) cbLibro.getSelectedItem();
        String tipoUsuario = (String) cbTipoUsuario.getSelectedItem();
        String cantidadStr = txtCantidad.getText().trim();

        // Regla 1: Campos obligatorios
        if (nombre.isEmpty() || rut.isEmpty() || cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El RUT, Nombre y Cantidad son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla 2: Cantidad número entero mayor que cero
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser un entero mayor que cero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad numérica válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla 3: Límite de libros y definición de días según el tipo de usuario
        int maxLibros = 0;
        int diasPrestamo = 0;

        switch (tipoUsuario) {
            case "Estudiante":
                maxLibros = 3; //
                diasPrestamo = 7;
                break;
            case "Docente":
                maxLibros = 5; //
                diasPrestamo = 15;
                break;
            case "Administrativo":
                maxLibros = 2; //
                diasPrestamo = 5;
                break;
        }

        if (cantidad > maxLibros) {
            JOptionPane.showMessageDialog(this, 
                "El tipo de usuario " + tipoUsuario + " solo puede solicitar un máximo de " + maxLibros + " libros.", 
                "Límite Excedido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmación de préstamo exitoso
        String mensajeExito = String.format(
            "¡Préstamo registrado exitosamente!\n\n" +
            "Usuario: %s\n" +
            "RUT: %s\n" +
            "Tipo: %s\n" +
            "Libro: %s\n" +
            "Cantidad: %d\n" +
            "Días de préstamo: %d días",
            nombre, rut, tipoUsuario, libro, cantidad, diasPrestamo
        );

        JOptionPane.showMessageDialog(this, mensajeExito, "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SistemaPrestamoLibros().setVisible(true);
        });
    }
}