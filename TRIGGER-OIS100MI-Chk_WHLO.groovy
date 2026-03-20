/**
 * README
 * Modification du dépôt en fonction de l'établissement, ou du type article
 * et du statut de la fiche article/dépôt lors de la création de la ligne de commande
 *
 * Name: Chk_WHLO
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création verbe Chk_WHLO sur API OIS100MI-AddOrderLine
 * 20231213   Ludovic Travers                Modification verbe Chk_WHLO sur API OIS100MI-AddOrderLine
 * 20240514   François LEPREVOST             Ajout d'une méthode pour appliquer le spécifique uniquement pour certains types de commande enregistrés dans CRS881/882
  */
public class Chk_WHLO extends ExtendM3Trigger {
  private final DatabaseAPI database
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private final TransactionAPI transaction
  private final LoggerAPI logger
  
  String company
  String societe
  String itty
  String faci
  String orno   
  String pyno   
  String whlo   
  String itno   
  String state10
  String state20
  String state30
  String whlohead   
  String statwhlohead
  
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
    
    orno = transaction.parameters.ORNO
    itno = transaction.parameters.ITNO
    List<String> listeCde = callOIS100MIGetBatchHead(orno)
    faci = listeCde[0]
    pyno = listeCde[1]
    String ortp = listeCde[2]
    logger.info("FRALEP ortp = " + ortp)
    
    if (!checkOrdertype(ortp)) {
      return;
    }
    
    pyno = pyno.trim()
    whlohead = callCRS610MIGetOrderInfo(pyno)

    if (faci != "null" && faci != null) {
      if (faci.substring(0,1)=="E") {
        itty = callMMS200MIGetItmBasic(itno)
        if (itty == "null" || itty == null) {
          itno = callMMS025MIGetItem(itno)
          if (itno == "null" || itno == null) {
            itno = transaction.parameters.ITNO
            itno = callMMS025MIGetItemLot(itno)
            itno = itno.trim()
           }
          itty = callMMS200MIGetItmBasic(itno)
        }
        
        if (itty == "LB ") {
          transaction.parameters.put("WHLO", "E11")
        } else { 
          whlo = whlohead
          statwhlohead = callMMS200MIGetItmWhsBasic(whlo, itno)
          whlo = "E10"
          state10 = callMMS200MIGetItmWhsBasic(whlo, itno)
          whlo = "E20"
          state20 = callMMS200MIGetItmWhsBasic(whlo, itno)
          whlo = "E30"
          state30 = callMMS200MIGetItmWhsBasic(whlo, itno)
          
          if ((statwhlohead == "20") || (statwhlohead == "50")) {
            transaction.parameters.put("WHLO", whlohead)
          } else {
            if ((state30 == "20") || (state30 == "50")) {
               transaction.parameters.put("WHLO", "E30")
            } else { 
              if (whlohead == "E10") {
                if ((state20 == "20") || (state20 == "50")) {
                  transaction.parameters.put("WHLO", "E20")
                } else { 
                  transaction.parameters.put("WHLO", "E10")
                }
              } else { 
                if ((state10 == "20") || (state10 == "50")) {
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
  * Call CRS881MI - on recherche si le type de commande est concerné par le spécifique.
  */
  private boolean checkOrdertype(String ortp) {
    Map<String, String> params = ["TRQF" : "1", "MSTD" : "TYPE_CDE", "MVRS" : "1", "BMSG" : "TypeCde", "IBOB" : "O", "ELMP" : "TypesCommandesExtModifDepot", "ELMD" : "TypesCommandesExtModifDepot", "MVXD" : ortp]
    boolean ok = false
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.MBMD != null && response.MBMD != "") {
        logger.info("FRALEP MBMD = " + response.MBMD)
        ok = true
      }
    }
    miCaller.call("CRS881MI", "GetTranslData", params, callback)
    logger.info("FRALEP ok = " + ok)
    return ok
  }
  
	/**
  * Call MMS025MI - GetItem to retrieve ITNO related to EAN13
  */
  private String callMMS025MIGetItem(String pitno) {
    String itno = ""
    Map<String, String> params = ["CONO" : company, "ALWT" : "02", "ALWQ" : "EA13", "POPN" : pitno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.ITNO != "") {
        itno = response.ITNO
      }
    }
    miCaller.call("MMS025MI", "GetItem", params, callback)
    return itno
  }      
  
  
	/**
  * Call MMS025MI - GetItem to retrieve ITNO related to Item LOT
  */
  private String callMMS025MIGetItemLot(String pitno) {
    String itno = ""
    Map<String, String> params = ["CONO" : company, "ALWT" : "01", "ALWQ" : "", "POPN" : pitno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.ITNO != "") {
        itno = response.ITNO
      }
    }
    miCaller.call("MMS025MI", "GetItem", params, callback)
    return itno
  }      
  
  /**
  * Call OIS100MI - GetBatchHead to retrieve FACI related to Order Number
  */
  private List<String> callOIS100MIGetBatchHead(String porno) {
    String faci = ""
    String CUNO = ""
    String ortp = ""
    Map<String, String> params = ["CONO" : company, "ORNO" : porno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.FACI != "") {
        faci = response.FACI
      }
      if (response.PYNO != "") {
        CUNO = response.PYNO
      }
       if (response.ORTP != "") {
        ortp = response.ORTP
      }
    }
    miCaller.call("OIS100MI", "GetBatchHead", params, callback)
    return [faci, CUNO, ortp]
  }      
      
  /**
  * Call CRS610MI - GetOrderInfo to retrieve WHLO related to Custumer Order 
  */
  private String callCRS610MIGetOrderInfo(String pCUNO) {
    String whlo = ""
    Map<String, String> params = ["CONO" : company, "CUNO" : pCUNO]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.WHLO != "") {
        whlo = response.WHLO
      }
    }
    miCaller.call("CRS610MI", "GetOrderInfo", params, callback)
    return whlo
  }      
      
  /**
  * Call MMS200MI - GetItemBasic to retrieve ITTY related to Item Number
  */
  private String callMMS200MIGetItmBasic(String pitno) {
    String itty = ""
    Map<String, String> params = ["CONO" : company, "ITNO" : pitno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.ITTY != "") {
        itty = response.ITTY
      }
    }
    miCaller.call("MMS200MI", "GetItmBasic", params, callback)
    return itty
  }      
  
  /**
  * Call MMS200MI - GetItmWhsBasic to retrieve STAT related to Item Number in WhareHouse 
  */
  private String callMMS200MIGetItmWhsBasic(String pwhlo, String pitno) {
    String STAT = ""
    Map<String, String> params = ["CONO" : company, "WHLO" : pwhlo, "ITNO" : pitno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.STAT != "") {
        STAT = response.STAT
      }
    }
    miCaller.call("MMS200MI", "GetItmWhsBasic", params, callback)
    return STAT
  }
}