package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.CONSTANTS;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class View_EngineOutput extends JEditorPane implements PropertyChangeListener {

    Model_JFXChess model;

    private String engineId = CONSTANTS.INTERNAL_ENGINE_NAME;
    private String depth = "";
    private String nps = "";
    private String hashFull = "";
    private String tbHits = "";
    private ArrayList<String> pvLines = new ArrayList<>();

    // This is the index of the first pv-line
    // among the children in txtEngineOut.
    private final int FirstPVLineChildIndex = 11;

    // This is the index of the first pv-line
    // in the split String infos.
    private final int FirstPVLineInfosIndex = 7;

    String htmlTest = "";

    public  View_EngineOutput(Model_JFXChess model) {
        this.model = model;

        // add empty engine line
        pvLines.add("");

        // set up formatting
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("body { font-family: sans-serif; }");
        css.addRule(
                "a { " +
                        "text-decoration: none; " +
                        "font-weight: normal; " +
                        "color: #333333; " +
                        "}"
        );
        setEditorKit(kit);
        setEditable(false);
        setFocusable(false);
        setContentType("text/html");

        htmlTest = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"4\" width=\"100%\">" +
                "  <tr>" +
                "    <td>" +
                "      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">" +
                "        <tr>" +
                "          <td>Stockfish (internal)</td>" +
                "          <td>e2e4 (depth 37/53)</td>" +
                "          <td>554.0 kn/s</td>" +
                "          <td>hashfull 1000</td>" +
                "          <td>tbhits 0</td>" +
                "        </tr>" +
                "      </table>" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>&nbsp;</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.18) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.19) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.19) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.19) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.19) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>" +
                "      (0.19) 1. e4 c6 2. d4 d5 3. exd5 cxd5 4. c4 Nf6 5. Nc3 Nc6 6. Bg5 Bf5 9. Bf4 Ng6 10. Bg3" +
                "    </td>" +
                "  </tr>" +
                "</table>";
        setText(htmlTest);
    }

    public void renderToHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"4\" width=\"100%\">" +
                "  <tr>" +
                "    <td>" +
                "      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"50%\">" +
                "        <tr>");
        s.append("<td>").append(engineId).append("</td>");
        s.append("<td>").append(depth).append("</td>");
        s.append("<td>").append(nps).append("</td>");
        s.append("<td>").append(hashFull).append("</td>");
        s.append("<td>").append(tbHits).append("</td>");
        s.append("        </tr>" +
                "      </table>" +
                "    </td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td>&nbsp;</td>" +
                "  </tr>");
        int firstNonEmptyPVIndex = pvLines.size() - 1;
        for (int i = pvLines.size()-1; i >= 0; i--) {
            if(!(pvLines.get(i).isEmpty())) {
                break;
            }
            firstNonEmptyPVIndex--;
        }
        //System.out.println(firstNonEmptyPVIndex);
        for(int i = firstNonEmptyPVIndex; i >= 0; i--) {
            s.append("<tr><td>");
            s.append(pvLines.get(firstNonEmptyPVIndex));
            s.append("</td></tr>");
        }
        s.append("</table>");
        //System.out.println(s.toString());
        //setText(s.toString());
        setText(htmlTest);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println("engine out view: property changed, new val: " + model.getCurrentEngineInfo());

        if(evt.getPropertyName().equals("engineInfo")) {
            //System.out.println("engine out view: property changed 1");


            String info = model.getCurrentEngineInfo();
            if (info != null && info.length() > 5) {
                String s = info.substring(5, info.length()).replace("ENGINE_ID", model.activeEngine.getName());
                //setText(info.substring(5, info.length()));
                setText(s);
                //System.out.println("engineview: "+info);
            }






/*

            //pv1.setText(info);
            // | id (Level MAX) | zobrist  |  nps | hashfull | tbhits | current Move + depth | eval+line pv1 | .. pv2 | ...pv3 | ...pv4 | ... | ...pv64 |

            // Note: All trailing empty matched strings will not
            // be part of infos, so we have to check infos.length().
            // But there can be empty strings "in between".
            String[] infos = info.split("\\|");

            if (infos.length > 1 && !infos[1].isEmpty()) {
                // we never update the engine id from the information
                // of the engine thread. instead we rely upon the mode
                // activation functions to set the proper name
                //engineId.setText(infos[1]);
            }
            if (infos.length > 3 && !infos[3].isEmpty()) {
                nps = infos[3];
            }
            if (infos.length > 4 && !infos[4].isEmpty()) {
                hashFull = infos[4];
            }
            if (infos.length > 5 && !infos[5].isEmpty()) {
                tbHits = infos[5];
            }
            if(infos.length > 6 && !infos[6].isEmpty()) {
                depth = infos[6];
            }

            // Set but don't clear the first pvLine-text.
            if(infos.length > FirstPVLineInfosIndex && !infos[ FirstPVLineInfosIndex].isEmpty()) {
                pvLines.set(0, infos[7]);
                System.out.println("info 0 " + infos[7]);
                //System.out.println("info 0l " + pvLines.size());
            }

            //pvLines.set(0, infos[7]);
            if(infos.length > 8) {
                System.out.println("info 1 " + infos[8]);
            }

            // Set the rest of the pvLine-texts or clear them if they are empty.
            for(int i=FirstPVLineInfosIndex+1;i<infos.length;i++) {
                if(pvLines.size() > i-FirstPVLineInfosIndex) {
                    if(!infos[i].isEmpty()) {
                        pvLines.set(i-FirstPVLineInfosIndex, infos[i]);
                        System.out.println("info i " + infos[i]);
                    } else {
                        pvLines.set(i-FirstPVLineInfosIndex, "");
                    }
                }
            }
            renderToHtml();

 */
        }



    }
}

