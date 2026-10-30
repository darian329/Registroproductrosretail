import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class SistemaCaja extends JFrame {

    private JTextField txtCodigoBusqueda, txtCantidad, txtDineroRecibido;
    private JLabel lblNombreProd, lblPrecioProd, lblStockProd, lblTotalPagar;
    private JTable tablaProductosCaja;
    private DefaultTableModel modeloTablaCaja;
    private JButton btnRealizarVenta, btnLimpiarCaja;
    
    private double precioActual = 0.0;
    private int stockActual = 0;
    private String nombreActual = "";

    public SistemaCaja() {
        setTitle("Sistema de Caja - Supermercado (Vendedor)");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior para caja
        JPanel panelVenta = new JPanel(new GridBagLayout());
        panelVenta.setBorder(BorderFactory.createTitledBorder("Caja Registradora - Cobro"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panelVenta.add(new JLabel("Código de Producto:"), gbc);
        gbc.gridx = 1; txtCodigoBusqueda = new JTextField(12); panelVenta.add(txtCodigoBusqueda, gbc);

        gbc.gridx = 2; gbc.gridy = 0; panelVenta.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 3; txtCantidad = new JTextField(8); txtCantidad.setText("1"); panelVenta.add(txtCantidad, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelVenta.add(new JLabel("Producto:"), gbc);
        lblNombreProd = new JLabel("---");
        lblNombreProd.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 1; panelVenta.add(lblNombreProd, gbc);

        gbc.gridx = 2; gbc.gridy = 1; panelVenta.add(new JLabel("Precio Unit.:"), gbc);
        lblPrecioProd = new JLabel("$0.00");
        gbc.gridx = 3; panelVenta.add(lblPrecioProd, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelVenta.add(new JLabel("Stock Disp.:"), gbc);
        lblStockProd = new JLabel("0");
        gbc.gridx = 1; panelVenta.add(lblStockProd, gbc);

        gbc.gridx = 2; gbc.gridy = 2; panelVenta.add(new JLabel("Total a Pagar:"), gbc);
        lblTotalPagar = new JLabel("$0.00");
        lblTotalPagar.setForeground(new Color(192, 57, 43));
        lblTotalPagar.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 3; panelVenta.add(lblTotalPagar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panelVenta.add(new JLabel("Dinero Recibido ($):"), gbc);
        gbc.gridx = 1; txtDineroRecibido = new JTextField(12); panelVenta.add(txtDineroRecibido, gbc);

        // Botones
        JPanel panelBotonesCaja = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRealizarVenta = new JButton("Cobrar / Vender");
        btnRealizarVenta.setBackground(new Color(46, 204, 113));
        btnRealizarVenta.setForeground(Color.WHITE);
        btnRealizarVenta.setFont(new Font("Arial", Font.BOLD, 12));

        btnLimpiarCaja = new JButton("Limpiar");
        panelBotonesCaja.add(btnRealizarVenta);
        panelBotonesCaja.add(btnLimpiarCaja);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        panelVenta.add(panelBotonesCaja, gbc);

        add(panelVenta, BorderLayout.NORTH);

        // Tabla de inventario para el cajero
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloTablaCaja = new DefaultTableModel(columnas, 0);
        tablaProductosCaja = new JTable(modeloTablaCaja);
        add(new JScrollPane(tablaProductosCaja), BorderLayout.CENTER);

        // Eventos
        txtCodigoBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                buscarProductoPorCodigo(txtCodigoBusqueda.getText().trim());
            }
        });

        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularTotal();
            }
        });

        btnRealizarVenta.addActionListener(e -> ejecutarVenta());
        btnLimpiarCaja.addActionListener(e -> limpiarCaja());

        tablaProductosCaja.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tablaProductosCaja.getSelectedRow();
                if (fila >= 0) {
                    String codigoSel = modeloTablaCaja.getValueAt(fila, 0).toString();
                    txtCodigoBusqueda.setText(codigoSel);
                    buscarProductoPorCodigo(codigoSel);
                }
            }
        });

        cargarInventarioCaja();
    }

    private void cargarInventarioCaja() {
        modeloTablaCaja.setRowCount(0);
        String sql = "SELECT * FROM productos";
        try (Connection con = Conexion.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    String.format("$%.2f", rs.getDouble("precio")),
                    rs.getInt("stock")
                };
                modeloTablaCaja.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar inventario: " + e.getMessage());
        }
    }

    private void buscarProductoPorCodigo(String codigo) {
        if (codigo.isEmpty()) {
            limpiarDatosProducto();
            return;
        }

        String sql = "SELECT * FROM productos WHERE codigo = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nombreActual = rs.getString("nombre");
                precioActual = rs.getDouble("precio");
                stockActual = rs.getInt("stock");

                lblNombreProd.setText(nombreActual);
                lblPrecioProd.setText(String.format("$%.2f", precioActual));
                lblStockProd.setText(String.valueOf(stockActual));
                calcularTotal();
            } else {
                limpiarDatosProducto();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calcularTotal() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double total = precioActual * cantidad;
            lblTotalPagar.setText(String.format("$%.2f", total));
        } catch (NumberFormatException e) {
            lblTotalPagar.setText("$0.00");
        }
    }

    private void ejecutarVenta() {
        String codigo = txtCodigoBusqueda.getText().trim();
        if (codigo.isEmpty() || nombreActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código de producto válido.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cantidadVenta = Integer.parseInt(txtCantidad.getText().trim());
            double dineroRecibido = Double.parseDouble(txtDineroRecibido.getText().trim());

            if (cantidadVenta <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cantidadVenta > stockActual) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente. Disponible: " + stockActual, "Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalPagar = precioActual * cantidadVenta;

            if (dineroRecibido < totalPagar) {
                JOptionPane.showMessageDialog(this, "El dinero recibido es insuficiente. Total: $" + totalPagar, "Dinero Insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double vuelto = dineroRecibido - totalPagar;
            int nuevoStock = stockActual - cantidadVenta;

            String sql = "UPDATE productos SET stock = ? WHERE codigo = ?";
            try (Connection con = Conexion.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, nuevoStock);
                ps.setString(2, codigo);
                ps.executeUpdate();

                String ticket = "=== TICKET DE VENTA (CAJA) ===\n" +
                                "Producto: " + nombreActual + "\n" +
                                "Código: " + codigo + "\n" +
                                "Cantidad: " + cantidadVenta + "\n" +
                                "Precio Unit.: $" + precioActual + "\n" +
                                "-----------------------------------\n" +
                                "Total a Pagar: $" + totalPagar + "\n" +
                                "Dinero Recibido: $" + dineroRecibido + "\n" +
                                "Vuelto Exacto: $" + String.format("%.2f", vuelto) + "\n" +
                                "===================================";

                JOptionPane.showMessageDialog(this, ticket, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);

                cargarInventarioCaja();
                limpiarCaja();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error en base de datos: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Verifique los campos numéricos (Cantidad y Dinero Recibido).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarDatosProducto() {
        nombreActual = "";
        precioActual = 0.0;
        stockActual = 0;
        lblNombreProd.setText("---");
        lblPrecioProd.setText("$0.00");
        lblStockProd.setText("0");
        lblTotalPagar.setText("$0.00");
    }

    private void limpiarCaja() {
        txtCodigoBusqueda.setText("");
        txtCantidad.setText("1");
        txtDineroRecibido.setText("");
        limpiarDatosProducto();
        tablaProductosCaja.clearSelection();
    }

    // MAIN INDEPENDIENTE PARA EL VENDEDOR
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemaCaja().setVisible(true));
    }
}