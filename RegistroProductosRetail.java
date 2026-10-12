import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistroProductosRetail extends JFrame {

    // Componentes del formulario
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox<String> cbCategoria;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregar;
    private JButton btnLimpiar;

    public RegistroProductosRetail() {
        // Configuración de la ventana principal
        setTitle("Empresa de Retail - Registro de Productos");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel de Formulario (Norte)
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Código del Producto
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Código del Producto:"), gbc);
        gbc.gridx = 1;
        txtCodigo = new JTextField(15);
        panelFormulario.add(txtCodigo, gbc);

        // 2. Nombre del Producto
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Nombre del Producto:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(15);
        panelFormulario.add(txtNombre, gbc);

        // 3. Categoría
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        String[] categorias = {"Electrónica", "Ropa y Moda", "Hogar", "Alimentos", "Juguetería"};
        cbCategoria = new JComboBox<>(categorias);
        panelFormulario.add(cbCategoria, gbc);

        // 4. Precio
        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Precio ($):"), gbc);
        gbc.gridx = 3;
        txtPrecio = new JTextField(15);
        panelFormulario.add(txtPrecio, gbc);

        // 5. Stock
        gbc.gridx = 2; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Stock Inicial:"), gbc);
        gbc.gridx = 3;
        txtStock = new JTextField(15);
        panelFormulario.add(txtStock, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAgregar = new JButton("Agregar Producto");
        btnLimpiar = new JButton("Limpiar Campos");
        panelBotones.add(btnAgregar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // Tabla de Registros (Centro)
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaProductos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Productos Registrados"));

        add(scrollTabla, BorderLayout.CENTER);

        // Eventos
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarYRegistrar();
            }
        });

        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });
    }

    private void validarYRegistrar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String categoria = (String) cbCategoria.getSelectedItem();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();

        // Regla 1: Campos obligatorios
        if (codigo.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla 2: Validar Precio (número mayor a cero)
        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser un número mayor a cero.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla 3: Validar Stock (entero mayor o igual a cero)
        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero válido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla 4: Verificar que el código no esté duplicado
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, 0).toString().equalsIgnoreCase(codigo)) {
                JOptionPane.showMessageDialog(this, "Ya existe un producto registrado con ese código.", "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Agregar fila a la tabla si todo está correcto
        Object[] fila = {codigo, nombre, categoria, String.format("$%.2f", precio), stock};
        modeloTabla.addRow(fila);

        JOptionPane.showMessageDialog(this, "Producto registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cbCategoria.setSelectedIndex(0);
        txtCodigo.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegistroProductosRetail().setVisible(true);
        });
    }
}