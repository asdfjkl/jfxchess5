package org.asdfjkl.jfxchess.gui;



import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.*;

public class View_MainFrame extends JFrame
        implements PropertyChangeListener {

    private final Model_JFXChess model;
    private final Controller_UI uiController;

    public View_MainFrame(Model_JFXChess model) {
        this.model = model;
        this.model.addListener(this);

        uiController = new Controller_UI(model);

        initUI();
    }

    private void initUI() {

        ArrayList<Image> icons = new ArrayList<>();

        icons.add(new ImageIcon(App.class.getResource("/icons/app_icon.png")).getImage());
        icons.add(new ImageIcon(App.class.getResource("/icons/app_icon@2x.png")).getImage());
        icons.add(new ImageIcon(App.class.getResource("/icons/app_icon@3x.png")).getImage());

        //JFrame frame = new JFrame("JFXChess");
        setTitle("JFXChess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setIconImages(icons);

        // ===== Menu Bar =====
        setJMenuBar(createMenuBar());

        // ===== Tool Bar =====
        JToolBar toolBar = createToolBar();

        toolBar.putClientProperty("JToolBar.isRollover", true);
        //btn.putClientProperty("JButton.buttonType", "toolBarButton");


        // ===== Main Content =====
        JComponent mainContent = createMainContent();

        // ===== Top Container (Toolbar + Content) =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(mainContent, BorderLayout.CENTER);

        setContentPane(topPanel);

    }

    // ----------------------------------------------------
    // Menu Bar
    // ----------------------------------------------------

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(new JMenuItem("New..."));
        gameMenu.add(new JMenuItem("Open File"));
        gameMenu.add(new JMenuItem("Save Game"));
        gameMenu.addSeparator();
        gameMenu.add(new JMenuItem("Print Game"));
        gameMenu.add(new JMenuItem("Print Position"));
        gameMenu.add(new JMenuItem("Save Position as Image"));
        gameMenu.addSeparator();
        gameMenu.add(new JMenuItem("Quit"));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("Copy Game"));
        editMenu.add(new JMenuItem("Copy Position (FEN)"));
        editMenu.add(new JMenuItem("Copy Position (Image)"));
        editMenu.add(new JMenuItem("Paste Game/Position"));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem("Edit Game Data"));
        editMenu.add(new JMenuItem("Enter Position"));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem("Flip Board"));

        JMenu modeMenu = new JMenu("Mode");
        modeMenu.add(new JMenuItem("Analysis"));
        modeMenu.add(new JMenuItem("Enter Moves"));
        modeMenu.add(new JMenuItem("Full Game Analysis"));
        modeMenu.add(new JMenuItem("Play Out Position"));
        modeMenu.addSeparator();
        modeMenu.add(new JMenuItem("Engines..."));
        modeMenu.add(new JMenuItem("Select Book"));

        JMenu viewMenu = new JMenu("View");
        viewMenu.add(new JMenuItem("Fullscreen"));
        viewMenu.add(new JMenuItem("Show Toolbar"));
        JMenu themeSubMenu = new JMenu("Theme");

        JMenuItem jmiToFlatlafLight = new JMenuItem("FlatLaf Light");
        jmiToFlatlafLight.addActionListener(uiController.switchLaf("com.formdev.flatlaf.FlatLightLaf"));
        themeSubMenu.add(jmiToFlatlafLight);

        JMenuItem jmiToFlatlafDark = new JMenuItem("FlatLaf Dark");
        jmiToFlatlafDark.addActionListener(uiController.switchLaf("com.formdev.flatlaf.FlatDarkLaf"));
        themeSubMenu.add(jmiToFlatlafDark);

        JMenuItem jmiToFlatlafIntellij = new JMenuItem("FlatLaf IJ");
        jmiToFlatlafIntellij.addActionListener(uiController.switchLaf("com.formdev.flatlaf.FlatIntelliJLaf"));
        themeSubMenu.add(jmiToFlatlafIntellij);

        JMenuItem jmiToFlatlafDarcula = new JMenuItem("FlatLaf Darcula");
        jmiToFlatlafDarcula.addActionListener(uiController.switchLaf("com.formdev.flatlaf.FlatDarculaLaf"));
        themeSubMenu.add(jmiToFlatlafDarcula);

        JMenuItem jmiToFlatlaMacLight = new JMenuItem("FlatLaf Fruit Light");
        jmiToFlatlaMacLight.addActionListener(uiController.switchLaf("com.formdev.flatlaf.themes.FlatMacLightLaf"));
        themeSubMenu.add(jmiToFlatlaMacLight);

        JMenuItem jmiToFlatlaMacDark = new JMenuItem("FlatLaf Fruit Dark");
        jmiToFlatlaMacDark.addActionListener(uiController.switchLaf("com.formdev.flatlaf.themes.FlatMacDarkLaf"));
        themeSubMenu.add(jmiToFlatlaMacDark);

        JMenuItem jmiToMetal = new JMenuItem("Swing Metal");
        jmiToMetal.addActionListener(uiController.switchLaf("javax.swing.plaf.metal.MetalLookAndFeel"));
        themeSubMenu.add(jmiToMetal);

        JMenuItem jmiToNimbus = new JMenuItem("Swing Nimbus");
        jmiToNimbus.addActionListener(uiController.switchLaf("javax.swing.plaf.nimbus.NimbusLookAndFeel"));
        themeSubMenu.add(jmiToNimbus);

        JMenuItem jmiToSysDefault = new JMenuItem("System Default");
        jmiToSysDefault.addActionListener(uiController.switchLaf("system.default"));
        themeSubMenu.add(jmiToSysDefault);

        viewMenu.add(themeSubMenu);
        viewMenu.add(new JMenuItem("Board & Pieces"));
        viewMenu.add(new JMenuItem("Reset Window Layout"));


        JMenu databaseMenu = new JMenu("Database");
        databaseMenu.add(new JMenuItem("Browse Database"));
        databaseMenu.add(new JMenuItem("Next Game"));
        databaseMenu.add(new JMenuItem("Previous Game"));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("About"));
        helpMenu.add(new JMenuItem("JFXChess Homepage"));

        menuBar.add(gameMenu);
        menuBar.add(editMenu);
        menuBar.add(modeMenu);
        menuBar.add(viewMenu);
        menuBar.add(databaseMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }


    // ----------------------------------------------------
    // Tool Bar
    // ----------------------------------------------------

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnTbNew = createToolButton("New Game", "open_in_new.svg");
        toolBar.add(btnTbNew);
        JButton btnTbOpen = createToolButton("Open File", "open_folder.svg");
        toolBar.add(btnTbOpen);
        JButton btnTbSave = createToolButton("Save Game", "file_save.svg");
        toolBar.add(btnTbSave);

        toolBar.addSeparator();

        JButton btnTbPrint = createToolButton("Print Game", "print.svg");
        toolBar.add(btnTbPrint);
        JButton btnTbFlip = createToolButton("Flip Board", "flip3.svg");
        toolBar.add(btnTbFlip);

        toolBar.addSeparator();

        JButton btnTbCopyGame = createToolButton("Copy Game", "copy1.svg");
        toolBar.add(btnTbCopyGame);
        JButton btnTbCopyPosition = createToolButton("Copy Position (FEN)", "copy2.svg");
        toolBar.add(btnTbCopyPosition);
        JButton btnTbPaste = createToolButton("Paste Game/Position", "paste.svg");
        toolBar.add(btnTbPaste);
        JButton btnTbSetupPosition = createToolButton("Setup Position", "setup_new_position.svg");
        toolBar.add(btnTbSetupPosition);

        toolBar.addSeparator();

        JButton btnTbFullAnalysis = createToolButton("Full Game Analysis", "game_analysis.svg");
        toolBar.add(btnTbFullAnalysis);

        toolBar.addSeparator();

        JButton btnTbBrowseDatabase = createToolButton("Browse Database", "database.svg");
        toolBar.add(btnTbBrowseDatabase);
        JButton btnTbDatabasePrevGame = createToolButton("Previous Game", "arrow_left_alt.svg");
        toolBar.add(btnTbDatabasePrevGame);
        JButton btnTbDatabaseNextGame = createToolButton("Next Game", "arrow_right_alt.svg");
        toolBar.add(btnTbDatabaseNextGame);

        toolBar.addSeparator();

        JButton btnTbAbout = createToolButton("About", "about.svg");
        toolBar.add(btnTbAbout);

        return toolBar;
    }

    private JButton createToolButton(String toolTipText, String fnIcon) {

        JButton btn = new JButton();
        String resIcn = "icons/" + fnIcon;
        btn.setIcon(new FlatSVGIcon(resIcn));
        btn.setToolTipText(toolTipText);
        btn.putClientProperty("JButton.buttonType", "toolBarButton");
        btn.setFocusable(false);

        return btn;
    }

    // ----------------------------------------------------
    // Main Content Area (Split Panes)
    // ----------------------------------------------------

    private JComponent createMainContent() {

        // ===== Left: Chessboard Placeholder =====
        View_Chessboard viewChessboard = new View_Chessboard(model);

        // ===== Right: Game Header Pane/Button + Text Pane + Nav Buttons =====

        // ===== Multiline Label =====
        JLabel lblGameHeader = new JLabel(
                "<html><div style='text-align:center;'>Kasparov, Garry - Karpov, Anatoly<br>" +
                        "Linares, 01.12.1987</div></html>"
        );

        lblGameHeader.setHorizontalAlignment(SwingConstants.CENTER);
        lblGameHeader.setVerticalAlignment(SwingConstants.CENTER);


        // ===== Button next to it =====
        JButton btnGameHeader = new JButton();
        btnGameHeader.putClientProperty("JButton.buttonType", "toolBarButton");
        btnGameHeader.setIcon(new FlatSVGIcon("icons/edit_game_header_18px.svg"));
        btnGameHeader.setToolTipText("Edit Game  Data");
        btnGameHeader.setFocusable(false);

        // ===== Header panel (Label + Button) =====
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));

        headerPanel.add(lblGameHeader, BorderLayout.CENTER);
        headerPanel.add(btnGameHeader, BorderLayout.EAST);

        headerPanel.setBorder(
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        );

        // ===== TextPane for the navPanel
        JTextPane rightTextPane = new JTextPane();
        JScrollPane rightScroll =
                new JScrollPane(rightTextPane);

        // Navigation buttons panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));


        JButton btnToStart = new JButton();
        btnToStart.putClientProperty("JButton.buttonType", "toolBarButton");
        btnToStart.setIcon(new FlatSVGIcon("icons/fast_rewind.svg"));
        btnToStart.setToolTipText("Edit Game  Data");
        btnToStart.setFocusable(false);

        JButton btnPrev = new JButton();
        btnPrev.putClientProperty("JButton.buttonType", "toolBarButton");
        btnPrev.setIcon(new FlatSVGIcon("icons/arrow_back.svg"));
        btnPrev.setToolTipText("Edit Game  Data");
        btnPrev.setFocusable(false);

        JButton btnNext = new JButton();
        btnNext.putClientProperty("JButton.buttonType", "toolBarButton");
        btnNext.setIcon(new FlatSVGIcon("icons/play_arrow.svg"));
        btnNext.setToolTipText("Edit Game  Data");
        btnNext.setFocusable(false);

        JButton btnToEnd = new JButton();
        btnToEnd.putClientProperty("JButton.buttonType", "toolBarButton");
        btnToEnd.setIcon(new FlatSVGIcon("icons/fast_forward.svg"));
        btnToEnd.setToolTipText("Edit Game  Data");
        btnToEnd.setFocusable(false);


        navPanel.add(btnToStart);
        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        navPanel.add(btnToEnd);

        // Container for right side
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        rightPanel.add(rightScroll, BorderLayout.CENTER);
        rightPanel.add(navPanel, BorderLayout.SOUTH);

        // ===== Horizontal Split (Board | Right Pane) =====
        JSplitPane horizontalSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                viewChessboard,
                rightPanel
        );

        horizontalSplit.setResizeWeight(0.7);
        horizontalSplit.setDividerLocation(600);
        horizontalSplit.setContinuousLayout(true);


        // ===== Engine On/Off, Thread Buttons etc. above Engine Info =====
        JPanel bottomControlBar = new JPanel(new BorderLayout());
        // --- Left side group ---
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        JToggleButton btnEngineSwitch = new JToggleButton("Start Engine");
        JButton btnAddLine = new JButton("+");
        JButton btnRemoveLine = new JButton("-");
        JButton btnThreads = new JButton("1 Threads(s)");

        btnEngineSwitch.setFocusable(false);
        btnAddLine.setFocusable(false);
        btnRemoveLine.setFocusable(false);
        btnThreads.setFocusable(false);

        leftGroup.add(btnEngineSwitch);
        leftGroup.add(btnAddLine);
        leftGroup.add(btnRemoveLine);
        leftGroup.add(btnThreads);

        // --- Right side group ---
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));

        //JButton btnEngines = new JButton("Engines...");

        JButton btnEngines = new JButton();
        //btnEngines.putClientProperty("JButton.buttonType", "toolBarButton");
        int h = btnEngineSwitch.getHeight();
        btnEngines.setIcon(new FlatSVGIcon("icons/engine_18px.svg"));
        btnEngines.setToolTipText("Select Engine");
        btnEngines.setFocusable(false);


        rightGroup.add(btnEngines);

        // --- Assemble ---
        bottomControlBar.add(leftGroup, BorderLayout.WEST);
        bottomControlBar.add(rightGroup, BorderLayout.EAST);

        // ===== Bottom Text Pane =====
        JTextPane bottomTextPane = new JTextPane();
        JScrollPane bottomScroll = new JScrollPane(bottomTextPane);

        // Container for bottom area
        JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.add(bottomControlBar, BorderLayout.NORTH);
        bottomPanel.add(bottomScroll, BorderLayout.CENTER);

        // ===== Vertical Split (Top | Bottom) =====
        JSplitPane verticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                horizontalSplit,
                bottomPanel
        );

        verticalSplit.setResizeWeight(0.8);
        verticalSplit.setDividerLocation(450);
        verticalSplit.setContinuousLayout(true);

        return verticalSplit;
    }

    private void setLookAndFeel(String lafClass) {

        if(lafClass.equals("system.default")) {
            lafClass = UIManager.getSystemLookAndFeelClassName();
        }

        // clear custom default font when switching to non-FlatLaf LaF
        if( !(UIManager.getLookAndFeel() instanceof FlatLaf) )
            UIManager.put( "defaultFont", null );

        try {
            UIManager.setLookAndFeel(lafClass);

            SwingUtilities.updateComponentTreeUI(this);

            invalidate();
            validate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("switchLaf".equals(evt.getPropertyName())) {
            setLookAndFeel(model.getLaf());
        }
    }
}

