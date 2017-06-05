
public class Player {
    String name;
    int hands;
    int folds;
    int raises;

    public Player(String name_) {
        name = name_;
        hands = 0;
        folds = 0;
        raises = 0;
    }

    public double fldprcnt() {
        return ((double)folds/(double)hands)*100;
    }

    public float raiseprcnt() { return ((raises/hands)*100); }

    public void hand_append(String tyyppi) {
        hands += 1;

        if (tyyppi.equals("folded")) {
            folds += 1;
        } else if (tyyppi.equals("raised")) {
            raises += 1;
        }
        System.out.println(name + "fold-%.: " + fldprcnt() +"  hands: " + hands +" folds: " + folds);
    }


}
