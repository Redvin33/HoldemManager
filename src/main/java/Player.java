public class Player {
    public String name;
    int hands;
    int folds;
    int raises;
    int buttons;
    int buttonraises;



    public Player(String name_) {
        name = name_;
        hands = 0;
        folds = 0;
        raises = 0;
        buttons = 0;
        buttonraises = 0;
        System.out.println("Created player " + name);
    }

    public double fldprcnt() {
        return ((double)folds/(double)hands)*100;
    }

    public float raiseprcnt() { return ((raises/hands)*100); }

    public double buttonraise() {
        if (buttons == 0) {
            return 0.0;
        }
        return ((double)buttonraises/(double)buttons)*100;
    }

    public void button(String tyyppi) {
        buttons += 1;
        if(tyyppi.equals("raises")) {
            buttonraises += 1;
        }
        hand_append(tyyppi);
    }

    public void hand_append(String tyyppi) {
        hands += 1;
        if (tyyppi.equals("folds")) {
            folds += 1;
        } else if (tyyppi.equals("raises")) {
            raises += 1;
        }
        System.out.println(name + " fold-%.: " + fldprcnt() +"  hands: " + hands +" folds: " + folds + " buttonraise: " + buttonraise() + " buttons: " + buttons);

    }

    public String getName() {
        return name;
    }

}