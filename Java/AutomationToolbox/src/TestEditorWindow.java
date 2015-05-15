package AutomationToolbox.src;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;


public class TestEditorWindow extends JFrame {
	private static final long serialVersionUID= 1L;
	private JTable mParamTable= null;
    private File mXMLFile;

	/**
	 * 
	 * @param testxml
	 */
	public TestEditorWindow( File testxml ) {
		this.mXMLFile= testxml;
		
		this.setTitle( "Test Info: " + this.mXMLFile.getName() );
		setBounds(200, 200, 751, 465);

		JLabel lblNewLabel = new JLabel( testxml.getAbsolutePath() );

		JButton btnOkay = new JButton("Okay");
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TestEditorWindow.this.setVisible( false );
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TestEditorWindow.this.setVisible( false );
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
				
		this.mParamTable= new JTable( new DefaultTableModel() );
		this.mParamTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel pane = new JLabel();
    			String s =  table.getModel().getValueAt(row, 0 ).toString();
                if( s.equals( "COMMON PARAMETERS" ) || s.equals( "TEST CASE PARAMETERS" ) ) {
                	pane.setOpaque( true );
                	pane.setBackground( Color.BLACK );
                	pane.setForeground( Color.WHITE );
                }
                else if( s.startsWith( "//" ) )
                	pane.setFont( new Font( "Courier", Font.ITALIC | Font.BOLD, 12 ) );

                pane.setText( (String)value );
                return pane;
            }
        });
		
		scrollPane.setViewportView(this.mParamTable);	
		
		JButton btnReveal = new JButton("Reveal");
		btnReveal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().open( TestEditorWindow.this.mXMLFile.getParentFile() );
				} catch( IOException e1 ) {
					e1.printStackTrace();
				}
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
					.addGap(6))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnReveal)
					.addGap(421)
					.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
					.addComponent(btnOkay, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
					.addGap(6))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(16)
					.addComponent(lblNewLabel)
					.addGap(12)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnCancel)
							.addComponent(btnReveal))
						.addComponent(btnOkay))
					.addGap(6))
		);
		getContentPane().setLayout(groupLayout);
		this._parse();
	}
	
	/**
	 * 
	 */
	void _parse() {
		// Initialize the columns
		((DefaultTableModel)this.mParamTable.getModel()).addColumn("Name");
		((DefaultTableModel)this.mParamTable.getModel()).addColumn("Type");
		((DefaultTableModel)this.mParamTable.getModel()).addColumn( "Test Case 1" );
    	((DefaultTableModel)this.mParamTable.getModel()).addRow( new Object[]{"COMMON PARAMETERS"});

    	SAXBuilder builder = new SAXBuilder(false);
        Document doc;
		try {
			doc= builder.build( this.mXMLFile );
		       //Get the root element
	        Element root= doc.getRootElement();
	        	        
			List<?> eCommonParams= root.getChildren( "Parameter" );
	        for( Object o : eCommonParams ) {
	        	Element param= (Element)o;
	        	((DefaultTableModel)this.mParamTable.getModel()).addRow( new Object[]{param.getAttribute( "name" ).getValue(), 
	        																		  param.getAttribute( "type" ).getValue(), 
	        																		  param.getAttribute( "value" ).getValue()});
	        }
	        
	        int nTCStartRow= -1;
        	((DefaultTableModel)this.mParamTable.getModel()).addRow( new Object[]{"TEST CASE PARAMETERS"}); 
			List<?> eTestcases= root.getChildren( "Testcase" );
	        for( int i= 0; i < eTestcases.size(); ++i ) {
	        	if( i+3 > ((DefaultTableModel)this.mParamTable.getModel()).getColumnCount() )
	        		((DefaultTableModel)this.mParamTable.getModel()).addColumn("Test Case " + (i+1));

				List<?> eTCParams= ((Element)eTestcases.get( i )).getChildren( "Parameter" );
		        for( int r= 0; r < eTCParams.size(); ++r ) {
		        	Element param= (Element)eTCParams.get( r );
		    		if( i == 0 ) {
			    		if( nTCStartRow == -1 )
			    			nTCStartRow= ((DefaultTableModel)this.mParamTable.getModel()).getRowCount();

		    			((DefaultTableModel)this.mParamTable.getModel()).addRow( new Object[]{param.getAttribute( "name" ).getValue(), 
		    																				  param.getAttribute( "type" ).getValue(), 
		        																		      param.getAttribute( "value" ).getValue()});
		    		}
		    		else
		    			((DefaultTableModel)this.mParamTable.getModel()).setValueAt( param.getAttribute( "value" ).getValue(), nTCStartRow+r, 2+i );
		        }
		    }

		} catch( JDOMException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}

	/**
	 * 
	 */
	static public HashMap<String, String> _createHTML( File fXMLFile, String mstrWebServerURL ) {
        HashMap<String, String> hmData= new HashMap<String, String>();
		String strHTMLData= "";
    	SAXBuilder builder = new SAXBuilder(false);
        Document doc;
		try {
			hmData.put( "Author", "Anonymous" );
			hmData.put( "Description", "None" );
			hmData.put( "Table", "" );
			
			String strTableHeader= 
					"<thead>\n" +
					"	<tr style=\"background-color:#000000;height:10px;cursor:pointer\">\n" +
					"		<th></th> <!-- empty header for action popup menu -->\n" +
					"		<th></th> <!-- name header -->\n";	
			
			String strDataParamNames= 
					"	<tr class=\"ui-widget-header\" style=\"white-space:nowrap;text-align:center;font-size:13px;\">\n" +
			        "		<td></td>\n" +
			        "		<td>&nbspTest Case Name&nbsp</td>\n";
	      
			String strDefaultValues= 
					"	<!-- Defaults -->\n" +
					"	<tr style=\"xoutline:thin solid;background-color:#bbbbbb;text-align:center\">\n" +
					"		<td id=\"defaults\">Defaults:</td>\n" +
					"		<td><i>*unique name</i></td>\n";

			String strTestCases= 
			        "	<!-- the real start of the testcases -->\n";

			if( fXMLFile != null ) {
				doc= builder.build( fXMLFile );
			    // Get the root element
		        Element root= doc.getRootElement();

		        LinkedHashMap<String, String> defaultParamSet= new LinkedHashMap<String, String>();
		        HashMap<String, String> paramType= new HashMap<String, String>();

				List<?> eCommonParams= root.getChildren( "Parameter" );
				List<?> eTestcases= root.getChildren( "Testcase" );

				// Get a complete list of all common and testcase params by doing a prescan of Common and Testcase params		        
				// Testcase params
		        for( int i= 0; i < eTestcases.size(); ++i ) {
					List<?> eTCParamsPre= ((Element)eTestcases.get( i )).getChildren( "Parameter" );
			        for( Object o : eTCParamsPre ) {
			        	Element param= (Element)o;
		        		defaultParamSet.put( param.getAttribute( "name" ).getValue(),
	        					  			 param.getAttribute( "value" ).getValue() );
		        		paramType.put( param.getAttribute( "name" ).getValue(), 
	        					   	   param.getAttribute( "type" ).getValue() );
			        }
		        }

				// Common params
		        for( Object o : eCommonParams ) {
		        	Element param= (Element)o;
	        		defaultParamSet.put( param.getAttribute( "name" ).getValue(),
	        					         param.getAttribute( "value" ).getValue() );
	        		paramType.put( param.getAttribute( "name" ).getValue(), 
	        					   param.getAttribute( "type" ).getValue() );
		        }

		        if( defaultParamSet.containsKey("author"))
	        		hmData.put( "Author", defaultParamSet.get("author") );
		        
		        if( defaultParamSet.containsKey("description"))
	        		hmData.put( "description", defaultParamSet.get("description") );
		        			        
		        // Create the table headers, param names and defaults
		        for (Entry<String, String> entry : defaultParamSet.entrySet()) {
		            String parameterName = entry.getKey();
	        		DatabaseMgr._DataParameters()._AddDataParameter(parameterName, entry.getValue(), paramType.get( parameterName ), false, "Imported from " + fXMLFile.getAbsolutePath());

		        	if( !parameterName.equals("testcaseName") ) {
			            String parameterValue= entry.getValue();			           	
		        		strDataParamNames+= "		<td id=\"" + parameterName + "\">&nbsp" + parameterName + " <img class=\"header-context-menu box menu-1\" src=\""+mstrWebServerURL+"/AutoManager/GetImage?AutomationToolbox/Preferences/Templates/Images/optbutton.png\" onmouseover=\"\" style=\"cursor: pointer;\" height=\"10\" width=\"10\">&nbsp</td>\n";
			        	strTableHeader+= "    	<th></th>\n";
			        	// Set default values
			            if( paramType.get( parameterName ).equalsIgnoreCase("Boolean"))
			            	strDefaultValues+= 	"		<td><select><option " + (parameterValue.equalsIgnoreCase("true")?"selected":"") + ">True</option><option " + (!parameterValue.equalsIgnoreCase("true")?"selected":"") + " disabled>False</option></select></td>\n";
			           	else
			            	strDefaultValues+= 	"		<td><input type=\"text\" style=\"width: 100%;\" id=\"paramvalue\" value=\"" + parameterValue + "\"></td>\n";
			        	// Now make all the default values italicized
			        	if( !parameterValue.isEmpty() )
			        		entry.setValue("<td default=\"true\"><i>"+parameterValue+"</i></td>");
		        	}
		        }
		        		        
		        // Parse the Testcases
		        for( int i= 0; i < eTestcases.size(); ++i ) {
		        	// Start with full copy of default values
			        LinkedHashMap<String, String> tcParamSet = new LinkedHashMap<String, String>(defaultParamSet);
			        // Start new testcase row
	        		strTestCases+= 
	        				"	<tr id=\"TheTableRow\" align=\"center\">\n" +
	    			        "		<td style=\"vertical-align: middle;\"> <img id=\"customImage\" class=\"row-context-menu box menu-1\" src=\""+mstrWebServerURL+"/AutoManager/GetImage?AutomationToolbox/Preferences/Templates/Images/optbutton.png\" onmouseover=\"\" style=\"cursor:pointer;vertical-align:middle;\" height=\"10\" width=\"10\"></td>\n";
	        		
					List<?> eTCParams= ((Element)eTestcases.get( i )).getChildren( "Parameter" );
					// Update the defaults with the testcase specific values
			        for( int r= 0; r < eTCParams.size(); ++r ) {
			        	Element param= (Element)eTCParams.get( r );
			        	tcParamSet.put(param.getAttribute( "name" ).getValue(), "<td>"+param.getAttribute( "value" ).getValue()+"</td>");
			        }

			        // testcaseName must be first column
	        		strTestCases+= "		" + tcParamSet.get("testcaseName") + "\n";
	        		// Now add everything else skipping testcaseName
	        		for (Entry<String, String> entry : tcParamSet.entrySet()) {
	        			if( !entry.getKey().equals("testcaseName") ) {
	        					strTestCases+= "		" + entry.getValue() + "\n";	        					
	        			}
			        }

			        strTestCases+= "	</tr>\n";
			    }
			}
	        
			// Close out the table header
			strTableHeader+= "		<th></th> <!-- type header -->\n	<tr>\n</thead\n"; 
			strDataParamNames+= "		<td> <img class=\"context-menu-one box menu-1\" src=\""+mstrWebServerURL+"/AutoManager/GetImage?AutomationToolbox/Preferences/Templates/Images/optbutton.png\" onmouseover=\"\" style=\"cursor: pointer;\" height=\"10\" width=\"10\"></td>\n	</tr>\n";
			strDefaultValues+= 	"		<td></td>\n	</tr>\n";
	        
	        // Now piece together the full table data
	        strHTMLData= strTableHeader + "<tbody>\n" + strDataParamNames + strDefaultValues + strTestCases;
	        
//	        strHTMLData= strTableHeader + "<tbody>\n";
//	        for (Object value : hmParams.values())
//		        strHTMLData+= value.toString(); 
	        strHTMLData+= "</tbody>\n";
	        
    		hmData.put( "Table", strHTMLData );

	        
		} catch( JDOMException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return hmData;
 	}
	
	/**
	 * 
	 * @param param
	 * @return
	 */
	 static String CreateRow( Element param ) {
		String strDisabled= " disabled";

    	String strRowData= "<tr>" +
				"<td><input type=\"text\" id=\"paramname\" value=\"" + param.getAttribute( "name" ).getValue() + "\"></td>" +
			  	"<td><select id=\"paramtype\">";
		for( String strType : new String[]{"String", "Boolean", "Float", "Double", "Int"} ) {
			if( strType.equalsIgnoreCase( param.getAttribute( "type" ).getValue() ) )
				strRowData+= "<option selected>" + strType + "</option>";
			else
				strRowData+= "<option" + strDisabled + ">" + strType + "</option>";
		}
		strRowData+= "</select></td>\n";
		
		// If boolean then add a True / False popup menu
		if( param.getAttribute( "type" ).getValue().equalsIgnoreCase( "Boolean" ) ) {
			strRowData+= "<td><select>";
			strRowData+= "<option" + (param.getAttribute( "value" ).getValue().equalsIgnoreCase( "True" ) ? " selected" : strDisabled) + ">True</option>";
			strRowData+= "<option" + (param.getAttribute( "value" ).getValue().equalsIgnoreCase( "True" ) ? strDisabled : " selected") + ">False</option></select></td>";
		}
		else	        		
			strRowData+= "<td><input type=\"text\" id=\"paramvalue\" style=\"display:table-cell; width:100%\" value=\"" + param.getAttribute( "value" ).getValue() + "\"></td>";
		
		strRowData+="</tr>\n";
	
		return strRowData;
	}
	 
	/**
	 * 
	 * @param param
	 * @return
	 */
	 static void AppendListRow( Element param, LinkedHashMap<String, String> hmParams, boolean bTestcase ) {
		String strDisabled= " disabled";
		String strParamName= param.getAttribute( "name" ).getValue();
		String strParamType= param.getAttribute( "type" ).getValue();
		String strParamValue= param.getAttribute( "value" ).getValue();
    	String strRowData= "";

    	// See if the DataParam already exists so we can append to it
		if( hmParams.containsKey( strParamName ) ) {
			strRowData= hmParams.get( strParamName );
			strRowData= strRowData.replace("</tr>", "");
			hmParams.remove( strParamName ); //TODO: yuck but this will keep all the testcase params grouped together
		}
		else // Create a new Dataparameter entry
		{				
			// Add the Dataparameter table row
			strRowData= "<tr>\n" +
			// Add the Dataparameter Action popup menu cell
			"<td><select id=\"dp_" + strParamName + "\"><option selected>Active</option><option>Inactive</option><option>Edit</option><option>Remove</option></select></td>\n" +
			// add the Dataparameter Name cell
			"<td>" + strParamName + "</td>\n" +					
			// Add the Dataparameter Type popup menu cell
			"<td>" + strParamType + "</td>\n";
						
			// Add an empty Global cell
			if( bTestcase )
				strRowData+= "<td align=\"center\"><i>None</i></td>\n";
		}
		
		// Add the DataParam value cell.
		// If boolean then add a True / False popup menu
		if( strParamType.equalsIgnoreCase( "Boolean" ) ) {
			strRowData+= "<td><select>";
			strRowData+= "<option" + (strParamValue.equalsIgnoreCase( "True" ) ? " selected" : strDisabled) + ">True</option>";
			strRowData+= "<option" + (strParamValue.equalsIgnoreCase( "True" ) ? strDisabled : " selected") + ">False</option>";
			strRowData+= "</select></td>";
		}
		else // String value	        		
			strRowData+= "<td><input type=\"text\" id=\"paramvalue\" style=\"display:table-cell; width:400\" value=\"" + strParamValue + "\"></td>";
		
		// Close the List item
		strRowData+="</tr>\n";

		//TODO: Hacky way to get the desired ordering
		LinkedHashMap<String, String> hmNewMap= (LinkedHashMap<String, String>)hmParams.clone();
		hmParams.clear();
		hmParams.put( strParamName, strRowData );
		hmParams.putAll( hmNewMap );
	}
}
