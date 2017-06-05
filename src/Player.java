
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

    public float fldprcnt() {
        return (folds/hands)*100;
    }

    public float raiseprcnt() {
        return(raises/hands)*100;
    }

    public void hand_append(String tyyppi) {
        hands += 1;

        if (tyyppi.equals("folded")) {
            folds += 1;
        } else if (tyyppi.equals("raised")) {
            raises += 1;
        }
        System.out.println(name + "fold-%.: " + fldprcnt());
    }


}
