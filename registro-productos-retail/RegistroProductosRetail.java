import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class RegistroProductosRetail extends JFrame {

    private JTextField txtCodigo, txtNombre, txtPrecio, txtStock;
    private JComboBox<String> cbCategoria;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregar, btnActualizar, btnEliminar, btnLimpiar;

    public RegistroProductosRetail() {
        setTitle("Empresa de Retail - Gestión de Productos (Administrador)");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panelFormulario.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1; txtCodigo = new JTextField(15); panelFormulario.add(txtCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelFormulario.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; txtNombre = new JTextField(15); panelFormulario.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelFormulario.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        String[] categorias = {"Electrónica", "Ropa y Moda", "Hogar", "Alimentos", "Juguetería"};
        cbCategoria = new JComboBox<>(categorias);
        panelFormulario.add(cbCategoria, gbc);

        gbc.gridx = 2; gbc.gridy = 0; panelFormulario.add(new JLabel("Precio ($):"), gbc);
        gbc.gridx = 3; txtPrecio = new JTextField(15); panelFormulario.add(txtPrecio, gbc);

        gbc.gridx = 2; gbc.gridy = 1; panelFormulario.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 3; txtStock = new JTextField(15); panelFormulario.add(txtStock, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnAgregar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        String[] columnas = {"Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false; // Los cambios se hacen en el formulario, no en la tabla
            }
        };
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Funciona con clic y con las flechas del teclado
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (e.getValueIsAdjusting() || fila < 0) return;

            txtCodigo.setText(modeloTabla.getValueAt(fila, 0).toString());
            txtCodigo.setEditable(false);
            txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
            String categoria = modeloTabla.getValueAt(fila, 2).toString();
            if (((DefaultComboBoxModel<String>) cbCategoria.getModel()).getIndexOf(categoria) < 0) {
                cbCategoria.addItem(categoria); // Categoría que no estaba en la lista
            }
            cbCategoria.setSelectedItem(categoria);
            txtPrecio.setText(modeloTabla.getValueAt(fila, 3).toString().replace("$", ""));
            txtStock.setText(modeloTabla.getValueAt(fila, 4).toString());
            btnAgregar.setEnabled(false); // Un producto ya existente se actualiza, no se guarda de nuevo
        });

        cargarProductos();
    }

    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        String sql = "SELECT * FROM productos";
        try (Connection con = Conexion.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    Moneda.formatear(rs.getBigDecimal("precio")),
                    rs.getInt("stock")
                };
                modeloTabla.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos.\nVerifique que Laragon (MySQL) esté encendido.\n\nDetalle: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarProducto() {
        if (!validarCampos()) return;

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String categoria = (String) cbCategoria.getSelectedItem();
        BigDecimal precio = Moneda.leer(txtPrecio.getText());
        int stock = Integer.parseInt(txtStock.getText().trim());

        String sql = "INSERT INTO productos (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, categoria);
            ps.setBigDecimal(4, precio);
            ps.setInt(5, stock);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");
            cargarProductos();
            limpiarCampos();

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Ya existe un producto con el código " + codigo + ".", "Código repetido", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el producto.\n\nDetalle: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        if (!hayProductoSeleccionado()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para actualizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCampos()) return;

        String sql = "UPDATE productos SET nombre=?, categoria=?, precio=?, stock=? WHERE codigo=?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, (String) cbCategoria.getSelectedItem());
            ps.setBigDecimal(3, Moneda.leer(txtPrecio.getText()));
            ps.setInt(4, Integer.parseInt(txtStock.getText().trim()));
            ps.setString(5, txtCodigo.getText().trim());

            if (ps.executeUpdate() == 0) {
                JOptionPane.showMessageDialog(this, "El producto ya no existe. Se recargará la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
                cargarProductos();
                limpiarCampos();
                return;
            }
            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            cargarProductos();
            limpiarCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        String codigo = txtCodigo.getText().trim();
        if (!hayProductoSeleccionado()) {
            JOptionPane.showMessageDialog(this, "Seleccione en la tabla el producto que desea eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opc = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el producto " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opc == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM productos WHERE codigo=?";
            try (Connection con = Conexion.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, codigo);
                if (ps.executeUpdate() == 0) {
                    JOptionPane.showMessageDialog(this, "El producto ya no existía.", "Aviso", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Producto eliminado.");
                }
                cargarProductos();
                limpiarCampos();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarCampos() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() ||
            txtPrecio.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (txtCodigo.getText().trim().length() > 50 || txtNombre.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El código admite hasta 50 caracteres y el nombre hasta 100.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        BigDecimal p = Moneda.leer(txtPrecio.getText());
        if (p == null || Moneda.tieneMasDeDosDecimales(p)) {
            JOptionPane.showMessageDialog(this, "Escriba el precio solo con números, por ejemplo 1500 o 1.500.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (p.signum() <= 0 || p.compareTo(new BigDecimal("100000000")) >= 0) {
            JOptionPane.showMessageDialog(this, "El precio debe ser mayor a 0 y menor a $100.000.000.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int s = Integer.parseInt(txtStock.getText().trim());
            if (s < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escriba el stock como un número entero, por ejemplo 10.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean hayProductoSeleccionado() {
        // El código queda bloqueado solo cuando se eligió un producto de la tabla
        return !txtCodigo.isEditable() && !txtCodigo.getText().trim().isEmpty();
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cbCategoria.setSelectedIndex(0);
        tablaProductos.clearSelection();
        btnAgregar.setEnabled(true);
    }
}