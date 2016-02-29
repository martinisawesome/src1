import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class MyListCellRenderer extends DefaultListCellRenderer{
	public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String label = (String) value;
        String[] sp = label.split("\n");
        String url = sp[0];
        String desc = sp[1];
        String labelText = "<html>Url: " + url+ "<br/>Desc: " + desc;
        setText(labelText);

        return this;
    }
}
