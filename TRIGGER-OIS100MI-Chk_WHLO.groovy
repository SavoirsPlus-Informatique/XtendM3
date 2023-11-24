/**
 * README
 * Modification du dépôt en fonction de l'établissement, ou du type article
 * et du statut de la fiche article/dépôt lors de la création de la ligne de commande
 *
 * Name: Chk_WHLO
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création verbe Chk_WHLO sur API OIS100MI-AddBatchLine
 */
public class Chk_WHLO extends ExtendM3Trigger {
  private final DatabaseAPI database
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private final TransactionAPI transaction
  private final LoggerAPI logger
  
  String company
  int CONO
  String societe
  String ITTY
  String FACI
  String ORNO   
  String PYNO   
  String WHLO   
  String ITNO   
  String STAT_E10
  String STAT_E20
  String STAT_E30
  String WHLO_Head   
  String FACI_Head   
  String STAT_WHLO_Head
  
  public Chk_WHLO(DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, 
          TransactionAPI transaction, LoggerAPI logger) {
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.transaction = transaction
    this.logger = logger
  }
      
  public void main() {
    company = program.getLDAZD().CONO
    societe = program.getLDAZD().DIVI
    
    ORNO = transaction.parameters.ORNO
    ITNO = transaction.parameters.ITNO
    List<String> listeCde = callOIS100MI_GetBatchHead(ORNO)
    FACI = listeCde[0]
    PYNO = listeCde[1]
    
    PYNO = PYNO.trim()
    WHLO_Head = callCRS610MI_GetOrderInfo(PYNO)

    if (FACI != "null" && FACI != null) {
      if (FACI.substring(0,1)=="E") {
        ITTY = callMMS200MI_GetItmBasic(ITNO)
        if (ITTY == "null" || ITTY == null) {
          ITNO = callMMS025MI_GetItem(ITNO)
          if (ITNO == "null" || ITNO == null) {
            ITNO = transaction.parameters.ITNO
            ITNO = callMMS025MI_GetItemLot(ITNO)
            ITNO = ITNO.trim()
           }
          ITTY = callMMS200MI_GetItmBasic(ITNO)
        }
        
        if (ITTY == "LB ") {
          transaction.parameters.put("WHLO", "E11")
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
            transaction.parameters.put("WHLO", WHLO_Head)
          } else {
            if ((STAT_E30 == "20") || (STAT_E30 == "50")) {
               transaction.parameters.put("WHLO", "E30")
            } else { 
              if (WHLO_Head == "E10") {
                if ((STAT_E20 == "20") || (STAT_E20 == "50")) {
                  transaction.parameters.put("WHLO", "E20")
                } else { 
                  transaction.parameters.put("WHLO", "E10")
                }
              } else { 
                if ((STAT_E10 == "20") || (STAT_E10 == "50")) {
                  transaction.parameters.put("WHLO", "E10")
                } else { 
                  transaction.parameters.put("WHLO", "E20")
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
  * Call OIS100MI - GetBatchHead to retrieve FACI related to Order Number
  */
  private List<String> callOIS100MI_GetBatchHead(String pORNO) {
    String FACI = ""
    String CUNO = ""
    def params = ["CONO" : company, "ORNO" : pORNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.FACI != "") {
        FACI = response.FACI
      }
      if (response.PYNO != "") {
        CUNO = response.PYNO
      }
    }
    miCaller.call("OIS100MI", "GetBatchHead", params, callback)
    return [FACI, CUNO]
  }      
      
  /**
  * Call CRS610MI - GetOrderInfo to retrieve WHLO related to Custumer Order 
  */
  private String callCRS610MI_GetOrderInfo(String pCUNO) {
    String WHLO = ""
    def params = ["CONO" : company, "CUNO" : pCUNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.WHLO != "") {
        WHLO = response.WHLO
      }
    }
    miCaller.call("CRS610MI", "GetOrderInfo", params, callback)
    return WHLO
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