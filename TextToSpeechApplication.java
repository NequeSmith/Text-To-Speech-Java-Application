//Doneque Smith

package tts;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TextToSpeechApplication 
{
    private JTextField inputField;
    private JButton speakButton;
    private JLabel statusLabel;
	private static Synthesizer synthesizer;
	 
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
				{
			public void run()
			{
				new TextToSpeechApplication().createAndDisplayGUI();
			}
				});
	}
	
    private void createAndDisplayGUI() 
    {
        JFrame frame = new JFrame("Text to Speech Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inputField = new JTextField(20);
        speakButton = new JButton("Convert Text to Speech");
        statusLabel = new JLabel();
        
        speakButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                String inputSource = inputField.getText();
                String inputText = "";

                if (inputSource.startsWith("http://") || inputSource.startsWith("https://")) 
                {
                    try 
                    {
                        inputText = getTextFromWebsite(inputSource);
                    } catch (IOException ex) 
                    {
                        System.out.println("Oh no! Seems there was an error when trying to retrieve the text from the website");
                        ex.printStackTrace();
                    }
                } 
                else 
                {
                    try 
                    {
                        inputText = getTextFromFile(inputSource);
                    } catch (IOException ex) 
                    {
                        System.out.println("Oh no! Seems there was an error when trying to retrieve the text from the file");
                        ex.printStackTrace();
                    }
                }

                if (inputText != null && !inputText.isEmpty()) 
                {
                    speakText(inputText);
                   
                } else 
                {
                    statusLabel.setText("There was an error when trying to retrieve the text from the source");
                }
            }
        });
        JPanel panel = new JPanel();
        panel.add(new JLabel("Please enter your file path or the website's URL: "));
        panel.add(inputField);
        panel.add(speakButton);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }	

    private void speakText(String text) 
    {
        // This performs lexical analysis on the input text provided that should be converted to speech
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // Speak each token
            speakToken(token);
            // Perform lexical analysis on each token and print the result
            performLexicalAnalysis(token);
        }
    }

    private void speakToken(String token) 
    {
        try {
            // If synthesizer is not allocated, allocate it
            if (synthesizer == null) 
            {
                System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
                Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
                synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(Locale.US));
                synthesizer.allocate();
                synthesizer.resume();
            }

            // speak the current token until the QUEUE becomes empty
            synthesizer.speakPlainText(token, null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private String getTextFromFile(String filePath) throws IOException 
    {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            text.append(line).append("\n");
        }
        reader.close();
        return text.toString();
    }

    private String getTextFromWebsite(String urlStr) throws IOException 
    {
        StringBuilder text = new StringBuilder();
        URL url = new URL(urlStr);
        try {
            Document document = Jsoup.connect(urlStr).timeout(5000).get();
            Elements paragraphsreader = document.select("p"); 
            Elements headingsreader = document.select("h1, h2, h3, h4, h5, h6"); 
            Elements listsreader = document.select("ul, ol, li");
            Elements tablesreader = document.select("table, th, tr, td");
            for (Element paragraph : paragraphsreader) 
            {
                text.append(paragraph.text()).append("\n");
            }
            for (Element heading : headingsreader) 
            {
                text.append(heading.text()).append("\n");
            }
            for (Element list : listsreader) 
            {
                text.append(list.text()).append("\n");
            }
            for (Element table : tablesreader) 
            {
                text.append(table.text()).append("\n");
            }
        } catch (SocketTimeoutException e) 
        {
            System.out.println("I'm sorry there was a time out error when trying to read data from the website");
            e.printStackTrace();
        }

        return text.toString();
    }

    private void performLexicalAnalysis(String token) 
    {
        if (isWord(token)) {
            System.out.println("Lexical Analysis: Word - " + token);
        } else if (isNumber(token)) {
            System.out.println("Lexical Analysis: Number - " + token);
        } else if (isSymbol(token)) {
            System.out.println("Lexical Analysis: Symbol - " + token);
        } else {
            System.out.println("Lexical Analysis: Other - " + token);
        }
    }

    private boolean isWord(String token) 
    {
        // Coded to include hyphenated words
    	return token.matches("[a-zA-Z\\-]+");
    }

    private boolean isNumber(String token) 
    {
        // Define your criteria for identifying numbers here
        // For example, you can check if it consists of digits
        return token.matches("\\d+");
    }

    private boolean isSymbol(String token) 
    {
        // Define your criteria for identifying symbols here
        // For example, you can check if it's a single character symbol
        return token.length() == 1 && !Character.isLetterOrDigit(token.charAt(0));
    }
    

        
}
