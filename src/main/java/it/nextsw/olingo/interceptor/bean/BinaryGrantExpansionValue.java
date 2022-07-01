package it.nextsw.olingo.interceptor.bean;

/**
 * Semplice bean che contiene le informazioni per concedere o meno il grant alle expansion
 * Created by f.longhitano on 11/07/2017.
 */
public class BinaryGrantExpansionValue {


        private String expansionName;
        private Boolean expansionGrant;

        public BinaryGrantExpansionValue(String expansionName, Boolean expansionGrant) {
            this.expansionName = expansionName;
            this.expansionGrant = expansionGrant;
        }

        public String getExpansionName() {
            return expansionName;
        }

        public void setExpansionName(String expansionName) {
            this.expansionName = expansionName;
        }

        public Boolean getExpansionGrant() {
            return expansionGrant;
        }

        public void setExpansionGrant(Boolean expansionGrant) {
            this.expansionGrant = expansionGrant;
        }

}
