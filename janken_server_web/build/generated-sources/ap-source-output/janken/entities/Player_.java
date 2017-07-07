package janken.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-05-09T12:12:49")
@StaticMetamodel(Player.class)
public class Player_ { 

    public static volatile SingularAttribute<Player, String> password;
    public static volatile SingularAttribute<Player, String> playerName;
    public static volatile SingularAttribute<Player, Integer> totalScore;

}