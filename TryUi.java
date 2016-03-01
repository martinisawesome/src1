import java.awt.*;
import java.awt.event.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class TryUi	extends JFrame
{
    // Instance attributes used in this example
    JPanel	main;
    JList<String>	listbox;
    JPanel top;
    JPanel bottom;
    TextField searchField;
    Button searchButton;

    public String searchQuery;
    private SearchActionListen getPrint;
    private java.util.List<String> queryResultArray;
    private String queryResultString;

    // Constructor of main frame
    public TryUi()
    {

        setLocation(400,300);
//        resize(new Dimension(1000, 1000));
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        main = new JPanel(new BorderLayout());
        main.setPreferredSize(new Dimension(1500, 500));
        main.setFont(new Font(main.getFont().getName(), main.getFont().getStyle(), 36));
        top = new JPanel(new GridLayout(1,2));
        top.setFont(new Font(top.getFont().getName(), top.getFont().getStyle(), 36));
        bottom = new JPanel();
        bottom.setFont(new Font(bottom.getFont().getName(), bottom.getFont().getStyle(), 36));
        searchButton = new Button("Search");
        searchField = new TextField();

        top.add(searchField);
        top.add(searchButton);

        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchQuery = searchField.getText();
                System.out.println(searchQuery);
                queryResultString = getPrint.getPrint(searchQuery);
                System.out.println(queryResultString);

                String listData[] = queryResultString.split(",");

                updateJlist(listData);


            }
        });

        searchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchQuery = searchField.getText();
                System.out.println(searchQuery);
                queryResultString = getPrint.getPrint(searchQuery);
                System.out.println(queryResultString);

                String listData[] = queryResultString.split(",");

                updateJlist(listData);


            }
        });

	    /*get the search keywords from above and search the results
	     * lets assume results are of the format below
	     */



        // Create a new listbox control
        listbox = new JList<String>();
        listbox.setFont(new Font(listbox.getFont().getName(), listbox.getFont().getStyle(), 36));
        listbox.setCellRenderer(new MyListCellRenderer());
        listbox.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    try {
                        String sel = listbox.getSelectedValue();
                        System.out.println("selected value is- " + sel);
                        String[] sp = sel.split("\n");
                        openWebPage(sp[0]);
                        //Desktop.getDesktop().browse(new URI("www.google.com"));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
        bottom.add(listbox);
        main.add(top,BorderLayout.NORTH);
        main.add(bottom,BorderLayout.CENTER);
        getContentPane().add(main);
        pack();
    }

    public static void openWebPage(String url){
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateJlist(String[] data) {
        DefaultListModel<String> listModel=new DefaultListModel<String>();
        for (int i=0; i<data.length; i++) {
            listModel.addElement(data[i]);
        }
        this.listbox.setModel(listModel);

    }

    // Main entry point for this example
    public static void main( String args[] )
    {
        // Create an instance of the test application
        TryUi mainFrame	= new TryUi();
        mainFrame.setVisible( true );
    }
}