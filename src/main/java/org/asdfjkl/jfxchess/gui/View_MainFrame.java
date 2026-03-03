package org.asdfjkl.jfxchess.gui;



import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.*;

public class View_MainFrame extends JFrame
        implements PropertyChangeListener {

    private final Model_JFXChess model;
    private final Controller_UI controller_UI;

    public JSplitPane horizontalSplit;
    public JSplitPane verticalSplit;

    /*
    private JRadioButtonMenuItem  jmiToFlatlafLight;
    private JRadioButtonMenuItem  jmiToFlatlafDark;
    private JRadioButtonMenuItem  jmiToFlatlafIntellij;
    private JRadioButtonMenuItem  jmiToFlatlafDarcula;
    private JRadioButtonMenuItem  jmiToFlatlaMacLight;
    private JRadioButtonMenuItem  jmiToFlatlaMacDark;
    private JRadioButtonMenuItem  jmiToMetal;
    private JRadioButtonMenuItem  jmiToNimbus;
    private JRadioButtonMenuItem  jmiToSysDefault;
    private JRadioButtonMenuItem  jmiBoardColorBlue;
    private JRadioButtonMenuItem  jmiBoardColorGreen;
    private JRadioButtonMenuItem  jmiBoardColorBrown;
    private JRadioButtonMenuItem  jmiPieceStyleMerida;
    private JRadioButtonMenuItem  jmiPieceStyleOld;
    private JRadioButtonMenuItem  jmiPieceStyleUSCF;
*/

    public View_MainFrame(Model_JFXChess model) {
        this.model = model;
        model.addListener(this);

        // todo: move to controller
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                model.saveModel();
                model.saveScreenGeometry();
            }
        });

        controller_UI = new Controller_UI(model);

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
        JMenuItem jmiCopyGame = new JMenuItem("Copy Game");
        jmiCopyGame.addActionListener(controller_UI.copyPgnToClipboard());
        editMenu.add(jmiCopyGame);
        JMenuItem jmiCopyFEN = new JMenuItem("Copy Position (FEN)");
        jmiCopyFEN.addActionListener(controller_UI.copyFenToClipboard());
        editMenu.add(jmiCopyFEN);

        JMenuItem jmiCopyImage = new JMenuItem("Copy Position (Image)");
        jmiCopyImage.addActionListener(controller_UI.copyBitmapToClipboard());
        editMenu.add(jmiCopyImage);

        editMenu.add(new JMenuItem("Paste Game/Position"));
        editMenu.addSeparator();

        JMenuItem jmiEditGameData = new JMenuItem("Edit Game Data");
        jmiEditGameData.addActionListener(controller_UI.editGameData());
        editMenu.add(jmiEditGameData);

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
        JMenu themeSubMenu = new JMenu("Theme");

        JRadioButtonMenuItem jmiToFlatlafLight = new JRadioButtonMenuItem("FlatLaf Light");
        jmiToFlatlafLight.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.FlatLightLaf"));
        themeSubMenu.add(jmiToFlatlafLight);
        jmiToFlatlafLight.setSelected(model.getLaf().equals("com.formdev.flatlaf.FlatLightLaf"));

        JRadioButtonMenuItem jmiToFlatlafDark = new JRadioButtonMenuItem("FlatLaf Dark");
        jmiToFlatlafDark.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.FlatDarkLaf"));
        themeSubMenu.add(jmiToFlatlafDark);
        jmiToFlatlafDark.setSelected(model.getLaf().equals("com.formdev.flatlaf.FlatDarkLaf"));

        JRadioButtonMenuItem jmiToFlatlafIntellij = new JRadioButtonMenuItem("FlatLaf IJ");
        jmiToFlatlafIntellij.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.FlatIntelliJLaf"));
        themeSubMenu.add(jmiToFlatlafIntellij);
        jmiToFlatlafIntellij.setSelected(model.getLaf().equals("com.formdev.flatlaf.FlatIntelliJLaf"));

        JRadioButtonMenuItem jmiToFlatlafDarcula = new JRadioButtonMenuItem("FlatLaf Darcula");
        jmiToFlatlafDarcula.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.FlatDarculaLaf"));
        themeSubMenu.add(jmiToFlatlafDarcula);
        jmiToFlatlafDarcula.setSelected(model.getLaf().equals("com.formdev.flatlaf.FlatDarculaLaf"));

        JRadioButtonMenuItem jmiToFlatlaMacLight = new JRadioButtonMenuItem("FlatLaf Fruit Light");
        jmiToFlatlaMacLight.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.themes.FlatMacLightLaf"));
        themeSubMenu.add(jmiToFlatlaMacLight);
        jmiToFlatlaMacLight.setSelected(model.getLaf().equals("com.formdev.flatlaf.themes.FlatMacLightLaf"));

        JRadioButtonMenuItem jmiToFlatlaMacDark = new JRadioButtonMenuItem("FlatLaf Fruit Dark");
        jmiToFlatlaMacDark.addActionListener(controller_UI.switchLaf("com.formdev.flatlaf.themes.FlatMacDarkLaf"));
        themeSubMenu.add(jmiToFlatlaMacDark);
        jmiToFlatlaMacDark.setSelected(model.getLaf().equals("com.formdev.flatlaf.themes.FlatMacDarkLaf"));

        JRadioButtonMenuItem jmiToMetal = new JRadioButtonMenuItem("Swing Metal");
        jmiToMetal.addActionListener(controller_UI.switchLaf("javax.swing.plaf.metal.MetalLookAndFeel"));
        themeSubMenu.add(jmiToMetal);
        jmiToMetal.setSelected(model.getLaf().equals("javax.swing.plaf.metal.MetalLookAndFeel"));

        JRadioButtonMenuItem jmiToNimbus = new JRadioButtonMenuItem("Swing Nimbus");
        jmiToNimbus.addActionListener(controller_UI.switchLaf("javax.swing.plaf.nimbus.NimbusLookAndFeel"));
        themeSubMenu.add(jmiToNimbus);
        jmiToNimbus.setSelected(model.getLaf().equals("javax.swing.plaf.nimbus.NimbusLookAndFeel"));

        JRadioButtonMenuItem jmiToSysDefault = new JRadioButtonMenuItem("System Default");
        jmiToSysDefault.addActionListener(controller_UI.switchLaf("system.default"));
        themeSubMenu.add(jmiToSysDefault);
        jmiToSysDefault.setSelected(model.getLaf().equals("system.default"));

        ButtonGroup grpUiTheme = new ButtonGroup();
        grpUiTheme.add(jmiToFlatlafLight);
        grpUiTheme.add(jmiToFlatlafDark);
        grpUiTheme.add(jmiToFlatlafIntellij);
        grpUiTheme.add(jmiToFlatlafDarcula);
        grpUiTheme.add(jmiToFlatlaMacLight);
        grpUiTheme.add(jmiToFlatlaMacDark);
        grpUiTheme.add(jmiToMetal);
        grpUiTheme.add(jmiToNimbus);
        grpUiTheme.add(jmiToSysDefault);

        viewMenu.add(themeSubMenu);

        JMenu boardColorMenu = new JMenu("Board Color");
        JRadioButtonMenuItem jmiBoardColorBlue = new JRadioButtonMenuItem("Blue");
        jmiBoardColorBlue.addActionListener(controller_UI.switchBoardColor(BoardStyle.STYLE_BLUE));
        boardColorMenu.add(jmiBoardColorBlue);
        jmiBoardColorBlue.setSelected(model.getBoardStyle().getColorStyle() == BoardStyle.STYLE_BLUE);

        JRadioButtonMenuItem jmiBoardColorGreen = new JRadioButtonMenuItem("Green");
        jmiBoardColorGreen.addActionListener(controller_UI.switchBoardColor(BoardStyle.STYLE_GREEN));
        boardColorMenu.add(jmiBoardColorGreen);
        jmiBoardColorGreen.setSelected(model.getBoardStyle().getColorStyle() == BoardStyle.STYLE_GREEN);

        JRadioButtonMenuItem jmiBoardColorBrown = new JRadioButtonMenuItem("Brown");
        jmiBoardColorBrown.addActionListener(controller_UI.switchBoardColor(BoardStyle.STYLE_BROWN));
        boardColorMenu.add(jmiBoardColorBrown);
        jmiBoardColorBrown.setSelected(model.getBoardStyle().getColorStyle() == BoardStyle.STYLE_BROWN);

        viewMenu.add(boardColorMenu);

        ButtonGroup grpUiBoardColor = new ButtonGroup();
        grpUiBoardColor.add(jmiBoardColorBlue);
        grpUiBoardColor.add(jmiBoardColorGreen);
        grpUiBoardColor.add(jmiBoardColorBrown);

        JMenu pieceStyleMenu = new JMenu("Piece Style");
        JRadioButtonMenuItem jmiPieceStyleMerida = new JRadioButtonMenuItem("Merida");
        jmiPieceStyleMerida.addActionListener(controller_UI.switchPieceStyle(BoardStyle.PIECE_STYLE_MERIDA));
        pieceStyleMenu.add(jmiPieceStyleMerida);
        jmiPieceStyleMerida.setSelected(model.getBoardStyle().getPieceStyle() == BoardStyle.PIECE_STYLE_MERIDA);

        JRadioButtonMenuItem jmiPieceStyleOld = new JRadioButtonMenuItem("Old");
        jmiPieceStyleOld.addActionListener(controller_UI.switchPieceStyle(BoardStyle.PIECE_STYLE_OLD));
        pieceStyleMenu.add(jmiPieceStyleOld);
        jmiPieceStyleOld.setSelected(model.getBoardStyle().getPieceStyle() == BoardStyle.PIECE_STYLE_OLD);

        JRadioButtonMenuItem jmiPieceStyleUSCF = new JRadioButtonMenuItem("USCF");
        jmiPieceStyleUSCF.addActionListener(controller_UI.switchPieceStyle(BoardStyle.PIECE_STYLE_USCF));
        pieceStyleMenu.add(jmiPieceStyleUSCF);
        jmiPieceStyleUSCF.setSelected(model.getBoardStyle().getPieceStyle() == BoardStyle.PIECE_STYLE_USCF);

        viewMenu.add(pieceStyleMenu);

        ButtonGroup grpUiPieceStyle = new ButtonGroup();
        grpUiPieceStyle.add(jmiPieceStyleMerida);
        grpUiPieceStyle.add(jmiPieceStyleOld);
        grpUiPieceStyle.add(jmiPieceStyleUSCF);

        JMenuItem jmiResetLayout = new JMenuItem("Reset Window Layout");
        jmiResetLayout.addActionListener(controller_UI.resetWindowLayout());
        viewMenu.add(jmiResetLayout);


        JMenu databaseMenu = new JMenu("Database");
        databaseMenu.add(new JMenuItem("Browse Database"));
        databaseMenu.add(new JMenuItem("Next Game"));
        databaseMenu.add(new JMenuItem("Previous Game"));

        JMenu helpMenu = new JMenu("Help");
        JMenuItem jmiAbout = new JMenuItem("About");
        jmiAbout.addActionListener(controller_UI.showAbout());
        helpMenu.add(jmiAbout);
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
        btnTbAbout.addActionListener(controller_UI.showAbout());
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
        Controller_Board controller_Board = new Controller_Board(model);
        View_Chessboard viewChessboard = new View_Chessboard(model, controller_UI, controller_Board);

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
        horizontalSplit = new JSplitPane(
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
        verticalSplit = new JSplitPane(
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

    public void setGeometry(ScreenGeometry g) {
        // restore screen geometry on main frame
        setSize(g.width, g.height);
        if(g.posX > 0 && g.posY > 0) {
            setLocation(g.posX, g.posY);
        }

        if (g.isMaximized) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        // Restore divider (after layout is ready)
        int dividerHorizontal = g.dividerHorizontal;
        int dividerVertical = g.dividerVertical;

        horizontalSplit.setDividerLocation(dividerHorizontal);
        verticalSplit.setDividerLocation(dividerVertical);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("switchLaf".equals(evt.getPropertyName())) {
            setLookAndFeel(model.getLaf());
        }
    }
}

