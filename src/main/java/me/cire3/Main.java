package me.cire3;

import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello LWJGL! - cire3");

        LaunchRenderDocDialog lr = new LaunchRenderDocDialog();
        lr.setLocationRelativeTo(null);
        lr.setVisible(true);
        lr.dispose();

        initGlfw();
        App.getInstance().run();
    }

    public static void initGlfw() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new RuntimeException("Failed to initialize GLFW!");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(800, 600, "LearnOpenGl", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create Window object!");
        }

        new App(window);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSetFramebufferSizeCallback(window, (window0, w, h) ->
                App.getInstance().framebufferSizeCallback(window0, w, h));
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    public static class LaunchRenderDocDialog extends JDialog {

        private static final long serialVersionUID = 8312760039213612790L;

        private final JPanel contentPanel = new JPanel();

        /**
         * Create the dialog.
         */
        public LaunchRenderDocDialog() {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon32.png"));
            setBounds(100, 100, 291, 103);
            setModal(true);
            setLocationByPlatform(true);
            setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
            setModalityType(ModalityType.TOOLKIT_MODAL);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setAlwaysOnTop(true);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            setTitle("LearnOpenGL: " + ManagementFactory.getRuntimeMXBean().getName());
            setResizable(false);
            getContentPane().setLayout(new BorderLayout());
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            getContentPane().add(contentPanel, BorderLayout.CENTER);
            contentPanel.setLayout(null);
            {
                JLabel lblNewLabel = new JLabel("Launch RenderDoc and press ok to continue...");
                lblNewLabel.setBounds(10, 11, 265, 14);
                contentPanel.add(lblNewLabel);
            }
            {
                JPanel buttonPane = new JPanel();
                buttonPane.setBackground(Color.WHITE);
                FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
                fl_buttonPane.setVgap(10);
                fl_buttonPane.setHgap(10);
                buttonPane.setLayout(fl_buttonPane);
                getContentPane().add(buttonPane, BorderLayout.SOUTH);
                {
                    JButton okButton = new JButton("OK");
                    okButton.setPreferredSize(new Dimension(60, 20));
                    okButton.setMargin(new Insets(0, 0, 0, 0));
                    okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            LaunchRenderDocDialog.this.setVisible(false);
                        }
                    });
                    okButton.setActionCommand("OK");
                    buttonPane.add(okButton);
                    getRootPane().setDefaultButton(okButton);
                }
                {
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.setPreferredSize(new Dimension(60, 20));
                    cancelButton.setMargin(new Insets(0, 0, 0, 0));
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.exit(0);
                        }
                    });
                    cancelButton.setActionCommand("Cancel");
                    buttonPane.add(cancelButton);
                }
            }

            JSeparator separator = new JSeparator();
            getContentPane().add(separator, BorderLayout.NORTH);
        }
    }
}