import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.Timer;
class Game{
    public static class Controller{
        final JFrame window;
        Model model;
        View view;
        //Constructor
        public Controller(Model model){
            this.window=new JFrame("Memory");//Create the window
            this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//close
            this.window.setResizable(false);//Disable the resize of the window
            this.reset(model);//Reset the game
        }
        //Reset the game
        public void reset(Model model){
            this.model=model;
            this.view=new View(model);
            this.window.setVisible(false);
            this.window.setContentPane(view);
            this.window.pack();
            this.window.setLocationRelativeTo(null);
            for(JButton button : this.model.getButtons()){
                button.addActionListener(new ButtonActionListner(this));
            }
            Utilities.timer(200,(ignored)->this.window.setVisible(true));//Show the window after 200ms
        }
        public JFrame getWindow(){
            return this.window;
        }
        public Model getModel(){
            return this.model;
        }
        public View getView(){
            return this.view;
        }
    }
    public static class Model{
        //Constants for the game
        static final String[] AVAILABLE_IMAGES=new String[]{"Action Kamen1.jpg","Bochan1.jpg","Himawari1.jpg","Kazama1.jpg","Masao1.jpg","Nani1.jpg","sen1.jpg","shinchan1.jpg","Shiroo1.jpg"};
        static final Integer MAX_REGISTERED_SCORES=10;
        final ArrayList<Float>scores;
        final ArrayList<JButton>buttons;
        final int columns;//Number of columns
        int tries;//Number of tries left
        boolean gameStarted;//Is the game started
        public Model(int columns){
            this.columns=columns;//Number of columns
            this.buttons=new ArrayList<>();//List of buttons in the game
            this.scores=new ArrayList<>();//List of scores in the game
            this.tries= 10;//Number of tries initially
            this.gameStarted = false;//Game is not started initially
            int numberOfImage = columns * columns;//Number of images
            Vector<Integer>v=new Vector<>();//Vector to store the images
            for(int i=0;i<numberOfImage - numberOfImage % 2;i++){ //Add the image
                v.add(i % (numberOfImage / 2));
            }
            if(numberOfImage % 2!=0) v.add(AVAILABLE_IMAGES.length-1);//Add
            //Add the images as a button to the game
            for(int i=0;i<numberOfImage;i++){
                int rand = (int)(Math.random() * v.size());//Randomly select an image
                String reference = AVAILABLE_IMAGES[v.elementAt(rand)];//Get the image
                this.buttons.add(new MemoryButton(reference));//Add the images as a button
                v.removeElement(rand);//Remove the image from the vector
            }
        }
        public int getColumns(){
         return columns;
        }   
        public ArrayList<JButton>getButtons(){
         return buttons;
        }
        //Get the number of tries left
        public int getTries(){
         return tries;
        }
        //Decrement the tries count by calling this method
        public void decrementTries(){
         this.tries--;
        }
        //return if the game has started
        public boolean isGameStarted(){
         return this.gameStarted;
        }
        //start the game
        public void startGame(){
         this.gameStarted = true;
        }
    }
    //class to handel the UI of the game
    public static class View extends JPanel{
    final JLabel tries;
    public View(Model model){
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.tries = new JLabel("",SwingConstants.CENTER);
        this.tries.setFont(new Font("MV Boli",Font.BOLD,30));
        this.tries.setForeground(Color.WHITE);

        JPanel ImagePanel = new JPanel();
        int columns = model.getColumns();
        ImagePanel.setLayout(new GridLayout(columns,columns));
        for(JButton button : model.getButtons()){
           ImagePanel.add(button);
        }
        this.setTries(model.getTries());
        JPanel triesPanel = new JPanel();
        triesPanel.add(this.tries);
        triesPanel.setAlignmentX(CENTER_ALIGNMENT);
        triesPanel.setBackground(new Color(0X8946A6));
        this.add(triesPanel);
        this.add(ImagePanel); 
      }
       public void setTries(int triesLeft){
        this.tries.setText("Tries left: "+triesLeft);
       }
    }
    //class to handel the button on the images
    public static class ReferencedIcon extends ImageIcon{
     final String reference;
     public ReferencedIcon(Image image, String reference){
        super(image);
        this.reference = reference;
       }
     public String getReference(){
        return reference;
      }
    }
    //class to handel the button on the images
    public static class MemoryButton extends JButton{
     static final String IMAGE_PATH = "";
     static final Image NO_IMAGE = Utilities.loadImage("what1.jpg");
     public MemoryButton(String reference){
        Image image = Utilities.loadImage(IMAGE_PATH + reference);
        Dimension dimension = new Dimension(120,120);
        this.setPreferredSize(dimension);
        this.setIcon(new ImageIcon(NO_IMAGE));
        this.setDisabledIcon(new ReferencedIcon(image,reference));
      }
    }
    public static class Dialogs{
      public static void showLoseDialog(JFrame window){
        UIManager.put("OptionPane.background",new Color(0XEA99D5));
        UIManager.put("Panel.background",new Color(0XEA99D5));
        JOptionPane.showMessageDialog(window, "You lost, try again!", "You lost", JOptionPane.INFORMATION_MESSAGE);
      }
     public static void showwinDialog(JFrame window,Model model){
        String message = String.format("Congrats you won!");
        UIManager.put("OptionPane.background",new Color(0XEA99D5));
        UIManager.put("Panel.background",new Color(0XEA99D5));
        JOptionPane.showMessageDialog(window.getContentPane(),message,"",JOptionPane.INFORMATION_MESSAGE);
     }
    }
    //class to handel to button clicks
    public static class ButtonActionListner implements ActionListener{
     final Controller controller;
     final Model model;
     final View view;
     final JFrame window;
     static int disabledbuttonCount =0;
     static JButton lastDisabledButton = null;
     static final Image TRAP_IMAGE = Utilities.loadImage("what1.jpg");
      final ReferencedIcon trap;
      public ButtonActionListner(Controller controller){
        this.controller = controller;
        this.model = controller.getModel();
        this.view = controller.getView();
        this.window = controller.getWindow();
        this.trap = new ReferencedIcon(TRAP_IMAGE,"what1.jpg");
      }
      //Method to handel the button clicks and check if two images are same
      @Override
      public void actionPerformed(ActionEvent e){
          JButton button = (JButton) e.getSource();
         button.setEnabled(false);
         ReferencedIcon thisIcon = (ReferencedIcon) button.getDisabledIcon();
         disabledbuttonCount++;
         if(!model.isGameStarted()){ //if the game has not started
            model.startGame();//Start the Game
         }
         if(disabledbuttonCount == 2){ //if two buttons are disabled
            ReferencedIcon thatIcon = (ReferencedIcon) lastDisabledButton.getDisabledIcon();
            boolean isPair = thisIcon.getReference().equals(thatIcon.getReference());
            if(!isPair){//if the two images are not the same
               model.decrementTries();//Decrement the number of tries
               view.setTries(model.getTries()); //Update the number of tries
               JButton lastButton = lastDisabledButton;//store the last button
               Utilities.timer(500,(ignored->{ //Wait 500ms before re-enabling the
                  button.setEnabled(true); //Re-enable the last button
                lastButton.setEnabled(true);//Re-enabled the last button
               }));
            }
            disabledbuttonCount = 0;//Reset the counter
         }
         ArrayList<JButton>enabledButtons = (ArrayList<JButton>)model.getButtons().stream().filter(Component::isEnabled).collect(Collectors.toList());
         if(enabledButtons.size() == 0){ //if all the buttons are disabled
           controller.reset(new Model(controller.getModel().getColumns()));//Reset
           Dialogs.showwinDialog(window,model);//Show the lose dialog
         }
         lastDisabledButton = button; //Store the last button
         if(model.getTries() == 0){//if the number of tries is 0
            controller.reset(new Model(controller.getModel().getColumns()));//Reset
            Dialogs.showLoseDialog(window);//Show the lose dialog
            Utilities.timer(1000,(ignored)->model.getButtons().forEach(btn -> btn.setEnabled(false)));//wait 1s before disabling 
         }
        }
    }
 public static class Utilities{
     static final ClassLoader cl = Utilities.class.getClassLoader();
      //Method to create a timer
      public static void timer(int delay , ActionListener listner){
      Timer t=new Timer(delay,listner);
      t.setRepeats(false);
      t.start();
     }
     //Method to load an image
     public static Image loadImage(String s){
         Image image = null;
         try{
            InputStream resourceStream = cl.getResourceAsStream(s);
            if(resourceStream!= null){
                ImageInputStream imageStream = ImageIO.createImageInputStream(resourceStream);
                image = ImageIO.read(imageStream);
            }
         } catch(IOException e){
            e.printStackTrace();
         }
         return image;
        }
    }
}
// Main class to run the game
class Main{
 static final int DEFAULT_SIZE = 4; 
 public static void main(String[] args){
        Locale.setDefault(Locale.ENGLISH);
        SwingUtilities.invokeLater(()-> new Game.Controller(new Game.Model(DEFAULT_SIZE)));
    }
}
