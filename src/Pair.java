import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Pair<K, V> {

    private final K element0;
    private final V element1;

    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public K getElement0() {
        return element0;
    }

    public V getElement1() {
        return element1;
    }
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof Pair))
            return false;
        if (obj == this)
            return true;
        //on regarde si les 2 éléments sont egaux
        Pair p = (Pair) obj;
        return new EqualsBuilder().
            append(element0,p.element0).
            append(element1, p.element1).
            isEquals();
        
        //le même hash code sera retourné si les 2 paires comparées sont égales
      }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(257, 31).append(element0).append(element1).toHashCode();
        		
           
    }


}