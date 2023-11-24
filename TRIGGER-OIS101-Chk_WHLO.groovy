/**
 * README
 * Modification du dépôt en fonction de l'établissement, ou du type article
 * et du statut de la fiche article/dépôt lors de la création de la ligne de commande
 *
 * Name: Chk_WHLO
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création classe Chk_WHLO sur OIS101/B
 */
public class Chk_WHLO extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  
  String company
  int CONO
  String societe
  String DIVI
  String WHLO
  String ITNO
  String ITTY
  String ORNO   
  String STAT_E10
  String STAT_E20
  String STAT_E30
  String STAT_WHLO_Head
  String WHLO_Head
  String FACI_Head
  String mode
 
  public Chk_WHLO(ProgramAPI program, InteractiveAPI interactive,
    DatabaseAPI database, MICallerAPI miCaller) {
    this.program = program
    this.interactive = interactive
    this.database = database
    this.miCaller = miCaller    
  }
  
  public void main() {
    def OHEAD = program.getTableRecord("OOHEAD")
    WHLO_Head = OHEAD.OAWHLO
    FACI_Head = OHEAD.OAFACI
    WHLO = interactive.display.fields.OBWHLO
    ORNO = interactive.display.fields.OAORNO
    ITNO = interactive.display.fields.WBITNO
      
    company = program.getLDAZD().CONO
    societe = program.getLDAZD().DIVI
    
    mode = interactive.getMode()
   
    if (mode != "change") {
      if (FACI_Head != "null" && FACI_Head != null) {
        if (FACI_Head.substring(0,1)=="E") {
          ITTY = callMMS200MI_GetItmBasic(ITNO)
          if (ITTY == "null" || ITTY == null) {
            ITNO = callMMS025MI_GetItem(ITNO)
            if (ITNO == "null" || ITNO == null) {
              ITNO = interactive.display.fields.WBITNO
              ITNO = callMMS025MI_GetItemLot(ITNO)
            }
            ITTY = callMMS200MI_GetItmBasic(ITNO)
          }
          if (ITTY == "LB ") {
            interactive.display.fields.put("OBWHLO", "E11")
          } else { 
            WHLO = WHLO_Head
            STAT_WHLO_Head = callMMS200MI_GetItmWhsBasic(WHLO, ITNO)
            WHLO = "E10"
            STAT_E10 = callMMS200MI_GetItmWhsBasic(WHLO, ITNO)
            WHLO = "E20"
            STAT_E20 = callMMS200MI_GetItmWhsBasic(WHLO, ITNO)
            WHLO = "E30"
            STAT_E30 = callMMS200MI_GetItmWhsBasic(WHLO, ITNO)
              
            if ((STAT_WHLO_Head == "20") || (STAT_WHLO_Head == "50")) {
              interactive.display.fields.put("OBWHLO", WHLO_Head)
            } else {
              if ((STAT_E30 == "20") || (STAT_E30 == "50")) {
                interactive.display.fields.put("OBWHLO", "E30")
              } else { 
                if (WHLO_Head == "E10") {
                  if ((STAT_E20 == "20") || (STAT_E20 == "50")) {
                    interactive.display.fields.put("OBWHLO", "E20")
                  } else { 
                    interactive.display.fields.put("OBWHLO", "E10")
                  }
                } else { 
                  if ((STAT_E10 == "20") || (STAT_E10 == "50")) {
                    interactive.display.fields.put("OBWHLO", "E10")
                  } else { 
                    interactive.display.fields.put("OBWHLO", "E20")
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
	/**
  * Call MMS025MI - GetItem to retrieve ITNO related to EAN13
  */
  private String callMMS025MI_GetItem(String pITNO) {
    String ITNO = ""
    def params = ["CONO" : company, "ALWT" : "02", "ALWQ" : "EA13", "POPN" : pITNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.ITNO != "") {
        ITNO = response.ITNO
      }
    }
    miCaller.call("MMS025MI", "GetItem", params, callback)
    return ITNO
  }      
  
  
	/**
  * Call MMS025MI - GetItem to retrieve ITNO related to Item LOT
  */
  private String callMMS025MI_GetItemLot(String pITNO) {
    String ITNO = ""
    def params = ["CONO" : company, "ALWT" : "01", "ALWQ" : "", "POPN" : pITNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.ITNO != "") {
        ITNO = response.ITNO
      }
    }
    miCaller.call("MMS025MI", "GetItem", params, callback)
    return ITNO
  }      
  
  
	/**
  * Call MMS200MI - GetItemBasic to retrieve ITTY related to Item Number
  */
  private String callMMS200MI_GetItmBasic(String pITNO) {
    String ITTY = ""
    def params = ["CONO" : company, "ITNO" : pITNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.ITTY != "") {
        ITTY = response.ITTY
      }
    }
    miCaller.call("MMS200MI", "GetItmBasic", params, callback)
    return ITTY
  }      
  
  /**
  * Call MMS200MI - GetItmWhsBasic to retrieve STAT related to Item Number in WhareHouse 
  */
  private String callMMS200MI_GetItmWhsBasic(String pWHLO, String pITNO) {
    String STAT = ""
    def params = ["CONO" : company, "WHLO" : pWHLO, "ITNO" : pITNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.STAT != "") {
        STAT = response.STAT
      }
    }
    miCaller.call("MMS200MI", "GetItmWhsBasic", params, callback)
    return STAT
  }
}