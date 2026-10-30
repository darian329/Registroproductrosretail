
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Taller Aplicado - Manejo de Eventos en Java Swing
 * Autor: Darian Molina
 * Universidad Bernardo O'Higgins (UBO)
 */
public class TallerEventosApp extends JFrame {

    // Componentes de la interfaz
    private JTextField txtNombre, txtPrecio, txtStock;
    private JComboBox<String> cmbCategoria;
    private JCheckBox chkDescuento;
    private JList<String> listaProductos;
    private DefaultListModel<String> modeloLista;
    private JLabel lblEstado;

    public TallerEventosApp() {
        // Configuración de la ventana principal
        setTitle("Sistema de Gestión y Venta - Taller de Eventos");
        setSize(550, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Superior: Formulario de Registro ---
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));

        panelFormulario.add(new JLabel("Nombre del Producto:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Precio Unitario ($):"));
        txtPrecio = new JTextField();
        panelFormulario.add(txtPrecio);

        panelFormulario.add(new JLabel("Stock Inicial:"));
        txtStock = new JTextField();
        panelFormulario.add(txtStock);

        panelFormulario.add(new JLabel("Categoría:"));
        cmbCategoria = new JComboBox<>(new String[]{"Seleccione...", "Electrónica", "Ropa", "Alimentos"});
        panelFormulario.add(cmbCategoria);

        panelFormulario.add(new JLabel("¿Aplica Descuento VIP (10%)?"));
        chkDescuento = new JCheckBox();
        panelFormulario.add(chkDescuento);

        add(panelFormulario, BorderLayout.NORTH);

        // --- Panel Central: Lista y Acciones ---
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        
        modeloLista = new DefaultListModel<>();
        listaProductos = new JList<>(modeloLista);
        panelCentro.add(new JScrollPane(listaProductos), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnRegistrar = new JButton("Registrar Producto");
        JButton btnVender = new JButton("Simular Venta");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnVender);

        panelCentro.add(panelBotones, BorderLayout.SOUTH);
        panelCentro.setBorder(BorderFactory.createTitledBorder("Inventario Actual"));
        add(panelCentro, BorderLayout.CENTER);

        // --- Panel Inferior: Barra de Estado ---
        lblEstado = new JLabel(" Estado: Listo para registrar productos.");
        lblEstado.setBorder(BorderFactory.createEtchedBorder());
        add(lblEstado, BorderLayout.SOUTH);

        // =========================================================================
        // IMPLEMENTACIÓN DE EVENTOS Y REGLAS DE NEGOCIO
        // =========================================================================

        // 1. ActionListener (con Expresión Lambda) para el botón Registrar
        btnRegistrar.addActionListener(e -> {
            registrarProducto();
        });

        // 2. ActionListener tradicional para el botón Vender
        btnVender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simularVenta();
            }
        });

        // 3. ItemListener para el ComboBox (Categoría)
        cmbCategoria.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String seleccion = (String) cmbCategoria.getSelectedItem();
                    lblEstado.setText(" Categoría seleccionada: " + seleccion);
                }
            }
        });

        // 4. ItemListener para el CheckBox (Descuento VIP) usando Lambda
        chkDescuento.addItemListener(e -> {
            boolean activo = (e.getStateChange() == ItemEvent.SELECTED);
            if (activo) {
                JOptionPane.showMessageDialog(this, "¡Descuento VIP del 10% activado para la próxima venta!", "Beneficio", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 5. MouseListener para la JList (interacción avanzada con el mouse)
        listaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Doble clic
                    int index = listaProductos.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String itemSeleccionado = modeloLista.getElementAt(index);
                        JOptionPane.showMessageDialog(null, "Detalle del ítem:\n" + itemSeleccionado, "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    // =========================================================================
    // MÉTODOS DE LÓGICA Y REGLAS DE NEGOCIO
    // =========================================================================

    private void registrarProducto() {
        // REGLA DE NEGOCIO 1: Validar campos obligatorios y formato numérico
        if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Todos los campos son obligatorios.", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cmbCategoria.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Error: Debe seleccionar una categoría válida.", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            // REGLA DE NEGOCIO 2: El precio y el stock deben ser valores positivos mayores a cero
            if (precio <= 0 || stock <= 0) {
                JOptionPane.showMessageDialog(this, "Error: El precio y el stock deben ser mayores a 0.", "Error de Rango", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Si pasa las validaciones, se agrega al JList
            String categoria = (String) cmbCategoria.getSelectedItem();
            String productoInfo = txtNombre.getText().trim() + " | Cat: " + categoria + " | Precio: $" + precio + " | Stock: " + stock;
            
            modeloLista.addElement(productoInfo);
            lblEstado.setText(" Estado: Producto registrado exitosamente.");
            
            // Limpiar campos
            txtNombre.setText("");
            txtPrecio.setText("");
            txtStock.setText("");
            cmbCategoria.setSelectedIndex(0);
            chkDescuento.setSelected(false);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: Ingrese valores numéricos válidos en Precio y Stock.", "Formato Incorrecto", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simularVenta() {
        int index = listaProductos.getSelectedIndex();
        // REGLA DE NEGOCIO 3: Debe seleccionar un producto existente de la lista para simular la venta
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la lista para vender.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String item = modeloLista.getElementAt(index);
        JOptionPane.showMessageDialog(this, "Venta procesada con éxito para:\n" + item, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
        lblEstado.setText(" Estado: Venta realizada del producto seleccionado.");
    }

    public static void main(String[] args) {
        // Ejecutar la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new TallerEventosApp().setVisible(true);
        });
    }
}