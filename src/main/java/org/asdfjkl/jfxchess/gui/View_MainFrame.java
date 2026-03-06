package org.asdfjkl.jfxchess.gui;



import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.*;
import org.asdfjkl.jfxchess.lib.CONSTANTS;
import org.asdfjkl.jfxchess.lib.HtmlPrinter;

public class View_MainFrame extends JFrame
        implements PropertyChangeListener {

    private final Model_JFXChess model;
    private final Controller_UI controller_UI;

    public JSplitPane horizontalSplit;
    public JSplitPane verticalSplit;

    JLabel lblGameHeader;

    JEditorPane rightEditorPane;

    HtmlPrinter htmlPrinter = new HtmlPrinter();
    String htmlString = "";

    private Object currentHighlight = null;

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

        JMenuItem jmiPaste =  new JMenuItem("Paste Game/Position");
        jmiPaste.addActionListener(controller_UI.pasteFenOrGame());
        editMenu.add(jmiPaste);
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
        lblGameHeader = new JLabel(
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
        rightEditorPane = new JEditorPane();
        // set up formatting
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        //css.addRule(".current-move { background-color: #ffeb3b; font-weight: bold; }");
        /*
        css.addRule(
                "a { " +
                        "text-decoration: none; " +   // removes underline
                        "font-weight: normal; " +     // removes bold
                        "color: #333333; " +          // optional: normal text color
                        "font-family: sans-serif; " +
                        "}"
        );
        */
        css.addRule("body { font-family: sans-serif; }");
        css.addRule(
                "a { " +
                        "text-decoration: none; " +
                        "font-weight: normal; " +
                        "color: #333333; " +
                        "}"
        );
        rightEditorPane.setEditable(false);

        //rightEditorPane.getCaret().setVisible(false);
        rightEditorPane.setFocusable(false);
        //rightEditorPane.getCaret().setSelectionVisible(false);
        //rightEditorPane.setCaret(null);
        rightEditorPane.setEditorKit(kit);
        rightEditorPane.setContentType("text/html");
        JScrollPane rightScroll =
                new JScrollPane(rightEditorPane);

        rightEditorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String id;
                if (e.getURL() != null)
                    id = e.getURL().getRef();
                else
                    id = e.getDescription().replace("#", "");
                controller_Board.goToNode(Integer.parseInt(id));
            }
        });

        rightEditorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightEditorRightClick(e);
                }
            }
        });

        // Navigation buttons panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));


        JButton btnToStart = new JButton();
        btnToStart.putClientProperty("JButton.buttonType", "toolBarButton");
        btnToStart.setIcon(new FlatSVGIcon("icons/fast_rewind.svg"));
        btnToStart.setToolTipText("Seek To Beginning");
        btnToStart.setFocusable(false);
        btnToStart.addActionListener(controller_Board.seekToBeginning());

        JButton btnPrev = new JButton();
        btnPrev.putClientProperty("JButton.buttonType", "toolBarButton");
        btnPrev.setIcon(new FlatSVGIcon("icons/arrow_back.svg"));
        btnPrev.setToolTipText("Move Back");
        btnPrev.setFocusable(false);
        btnPrev.addActionListener(controller_Board.moveBack());

        JButton btnNext = new JButton();
        btnNext.putClientProperty("JButton.buttonType", "toolBarButton");
        btnNext.setIcon(new FlatSVGIcon("icons/play_arrow.svg"));
        btnNext.setToolTipText("Move Forward");
        btnNext.setFocusable(false);
        btnNext.addActionListener(controller_Board.moveForward());

        JButton btnToEnd = new JButton();
        btnToEnd.putClientProperty("JButton.buttonType", "toolBarButton");
        btnToEnd.setIcon(new FlatSVGIcon("icons/fast_forward.svg"));
        btnToEnd.setToolTipText("Seek to End");
        btnToEnd.setFocusable(false);
        btnToEnd.addActionListener(controller_Board.seekToEnd());


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

    private void handleRightEditorRightClick(MouseEvent e) {
            JEditorPane pane = (JEditorPane) e.getSource();
            int pos = pane.viewToModel2D(e.getPoint());

            if (pos >= 0) {
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                Element element = doc.getCharacterElement(pos);
                AttributeSet attrs = element.getAttributes();

                AttributeSet anchor =
                        (AttributeSet) attrs.getAttribute(HTML.Tag.A);

                if (anchor != null) {
                    String href = (String) anchor.getAttribute(HTML.Attribute.HREF);

                    if (href != null) {
                        int nodeId = Integer.parseInt(href.substring(1));
                        showContextMenu(e, nodeId);
                    }
                }
            }
    }

    private void doSomething(String href) {
        System.out.println("Clicked link: " + href);

    }

    private void showContextMenu(MouseEvent e, int nodeId) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem addEditComment = new JMenuItem("Add/Edit Comment");
        contextMenu.add(addEditComment);
        JMenuItem deleteComment = new JMenuItem("Delete Comment");
        contextMenu.add(deleteComment);

        JMenu moveAnnotation = new JMenu("Move Annotation");
        JMenuItem blunder = new JMenuItem("?? Blunder");
        JMenuItem mistake = new JMenuItem("? Mistake");
        JMenuItem dubiousMove = new JMenuItem("?! Dubious Move");
        JMenuItem interestingMove = new JMenuItem("!? Interesting Move");
        JMenuItem goodMove = new JMenuItem("! Good Move");
        JMenuItem brilliantMove = new JMenuItem("!! Brilliant Move");
        JMenuItem noMoveAnnotation = new JMenuItem("No Move Annotation");
        moveAnnotation.add(blunder);
        moveAnnotation.add(mistake);
        moveAnnotation.add(dubiousMove);
        moveAnnotation.add(interestingMove);
        moveAnnotation.add(goodMove);
        moveAnnotation.add(brilliantMove);
        moveAnnotation.add(noMoveAnnotation);
        contextMenu.add(moveAnnotation);

        JMenu posAnnotation = new JMenu("Position Annotation");
        JMenuItem unclear = new JMenuItem("∞ Unclear");
        JMenuItem drawish = new JMenuItem("= Equal");
        JMenuItem slightAdvantageWhite = new JMenuItem("⩲ Slight Advantage White");
        JMenuItem slightAdvantageBlack = new JMenuItem("⩱ Slight Advantage Black");
        JMenuItem advantageWhite = new JMenuItem("+- Advantage White");
        JMenuItem advantageBlack = new JMenuItem("-+ Advantage Black");
        JMenuItem noPosAnnotation = new JMenuItem("No Position Annotation");
        posAnnotation.add(unclear);
        posAnnotation.add(drawish);
        posAnnotation.add(slightAdvantageWhite);
        posAnnotation.add(slightAdvantageBlack);
        posAnnotation.add(advantageWhite);
        posAnnotation.add(advantageBlack);
        posAnnotation.add(noPosAnnotation);
        contextMenu.add(posAnnotation);

        JMenuItem removeAnnotation = new JMenuItem("Remove Annotations");
        contextMenu.add(removeAnnotation);
        contextMenu.addSeparator();

        JMenuItem moveVariantUp = new JMenuItem("Move Variant Up");
        JMenuItem moveVariantDown = new JMenuItem("Move Variant Down");
        JMenuItem deleteVariant = new JMenuItem("Delete Variant");
        JMenuItem deleteFromHere = new JMenuItem("Delete From Here");
        contextMenu.add(moveVariantUp);
        contextMenu.add(moveVariantDown);
        contextMenu.add(deleteVariant);
        contextMenu.add(deleteFromHere);
        contextMenu.addSeparator();

        JMenuItem deleteAllComments = new JMenuItem("Delete All Comments");
        JMenuItem deleteAllVariants = new JMenuItem("Delete All Variants");
        contextMenu.add(deleteAllComments);
        contextMenu.add(deleteAllVariants);

        contextMenu.show(e.getComponent(), e.getX(), e.getY());

        addEditComment.addActionListener(ae -> {
            System.out.println("FOOBAR!!!");
        });
        deleteComment.addActionListener(controller_UI.deleteComment(nodeId));
        blunder.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_BLUNDER));
        mistake.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_MISTAKE));
        dubiousMove.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_DUBIOUS_MOVE));
        interestingMove.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_SPECULATIVE_MOVE));
        goodMove.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_GOOD_MOVE));
        brilliantMove.addActionListener(controller_UI.addMoveAnnotation(nodeId, CONSTANTS.NAG_BRILLIANT_MOVE));
        noMoveAnnotation.addActionListener(controller_UI.removeMoveAnnotations(nodeId));

        unclear.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_DRAWISH_POSITION));
        drawish.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_DRAWISH_POSITION));
        slightAdvantageWhite.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_WHITE_SLIGHT_ADVANTAGE));
        slightAdvantageBlack.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_BLACK_SLIGHT_ADVANTAGE));
        advantageWhite.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_WHITE_DECISIVE_ADVANTAGE));
        advantageBlack.addActionListener(controller_UI.addPosAnnotation(nodeId, CONSTANTS.NAG_BLACK_DECISIVE_ADVANTAGE));
        noPosAnnotation.addActionListener(controller_UI.removePosAnnotations(nodeId));
        removeAnnotation.addActionListener(controller_UI.removeMoveAndPosAnnotations(nodeId));

        moveVariantUp.addActionListener(controller_UI.moveVariantUp(nodeId));
        moveVariantDown.addActionListener(controller_UI.moveVariantDown(nodeId));
        deleteVariant.addActionListener(controller_UI.deleteVariant(nodeId));
        deleteFromHere.addActionListener(controller_UI.deleteFromHere(nodeId));

        deleteAllComments.addActionListener(controller_UI.deleteAllComments());
        deleteAllVariants.addActionListener(controller_UI.deleteAllVariants());

        /*
        JMenuItem item = new JMenuItem("Do something with " + href);
        item.addActionListener(ev -> doSomething(href));

        menu.add(item);
        menu.show(e.getComponent(), e.getX(), e.getY());
         */
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

    private void updatePgnHeaders() {
        // update label
        HashMap<String, String> pgnHeaders = model.getGame().getPgnHeaders();
        String newGameInfo = "<html><div style='text-align:center;'>" +
                pgnHeaders.get("White") + " - " +
                pgnHeaders.get("Black") + "<br>" +
                pgnHeaders.get("Site");
        if(!pgnHeaders.get("Date").isEmpty()) {
            newGameInfo = newGameInfo + ", " + pgnHeaders.get("Date");
        }
        newGameInfo += "</div></html>";
        lblGameHeader.setText(newGameInfo);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("switchLaf".equals(evt.getPropertyName())) {
            setLookAndFeel(model.getLaf());
        }
        if ("pgnHeadersChanged".equals(evt.getPropertyName())) {
            updatePgnHeaders();
        }
        if("currentGameNodeChanged".equals(evt.getPropertyName())) {
            /* working, but old version
            int id = model.getGame().getCurrentNode().getId();
            if(htmlString.isEmpty()) {
                htmlString =  htmlPrinter.printGame(model.getGame());
            } else {
                htmlString =
                htmlString.replaceAll(" style=\"background: silver\" ", "")
                        .replace("id=\"n" + id + "\"",
                                "id=\"n" + id + "\" style=\"background: silver\" ");
            }
            System.out.println(htmlString);
            rightEditorPane.setText(htmlString);
             */

            try {
                int id = model.getGame().getCurrentNode().getId();
                HTMLDocument doc = (HTMLDocument) rightEditorPane.getDocument();
                Element element = doc.getElement("n" + id);

                Highlighter highlighter = rightEditorPane.getHighlighter();

                if (element == null) {
                    // if root node, remove annotation before returning
                    if(model.getGame().getCurrentNode() == model.game.getRootNode()) {
                        highlighter.removeHighlight(currentHighlight);
                    }
                    return;
                }

                int start = element.getStartOffset();
                int end = element.getEndOffset();

                if (currentHighlight != null) {
                    highlighter.removeHighlight(currentHighlight);
                }

                currentHighlight = highlighter.addHighlight(
                        start,
                        end,
                        new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY)
                );

                Rectangle r = rightEditorPane.modelToView(start);

                if (r != null) {
                    rightEditorPane.scrollRectToVisible(r);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        if("gameChanged".equals(evt.getPropertyName()) || "treeChanged".equals(evt.getPropertyName())) {
            htmlString =  htmlPrinter.printGame(model.getGame());
            rightEditorPane.setText(htmlString);
            updatePgnHeaders();
        }
    }

}

