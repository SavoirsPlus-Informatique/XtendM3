/**
 * README
 * Modification du dépôt en fonction de l'établissement, ou du type article
 * et du statut de la fiche article/dépôt lors de la création de la ligne de commande
 *
 * Name: Chk_WHLO
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création classe Chk_WHLO sur OIS101/B
 * 20231213   Ludovic Travers                Modification classe Chk_WHLO sur OIS101/B
 * 20240514   François LEPREVOST             Ajout d'une méthode pour appliquer le spécifique uniquement pour certains types de commande enregistrés dans CRS881/882
 */
public class Chk_WHLO extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final LoggerAPI logger
  
  String company
  String societe
  String divi
  String whlo
  String itno
  String itty
  String orno   
  String state10
  String state20
  String state30
  String statwhlohead
  String whlohead
  String facihead
 
  public Chk_WHLO(ProgramAPI program, InteractiveAPI interactive,
    DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger) {
    this.program = program
    this.interactive = interactive
    this.database = database
    this.miCaller = miCaller    
    this.logger = logger
  }
  
  public void main() {
    TableRecordAPI OHEAD = program.getTableRecord("OOHEAD")
    whlohead = OHEAD.OAWHLO
    facihead = OHEAD.OAFACI
    whlo = interactive.display.fields.OBWHLO
    orno = interactive.display.fields.OAORNO
    itno = interactive.display.fields.WBITNO
    String ortp = interactive.display.fields.OAORTP
    logger.info("FRALEP ortp = " + ortp)
    
    if (!checkOrdertype(ortp)) {
      return;
    }
      
    company = program.getLDAZD().CONO
    societe = program.getLDAZD().DIVI
   
    if (facihead != "null" && facihead != null) {
    	if (facihead.substring(0,1)=="E") {
    	  itty = callMMS200MIGetItmBasic(itno)
    	  if (itty == "null" || itty == null) {
      		itno = callMMS025MIGetItem(itno)
      		if (itno == "null" || itno == null) {
      		  itno = interactive.display.fields.WBITNO
      		  itno = callMMS025MIGetItemLot(itno)
      		}
      		itty = callMMS200MIGetItmBasic(itno)
    	  }
    	  if (itty == "LB ") {
    		  interactive.display.fields.put("OBWHLO", "E11")
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
      		  interactive.display.fields.put("OBWHLO", whlohead)
      		} else {
      		  if ((state30 == "20") || (state30 == "50")) {
      			  interactive.display.fields.put("OBWHLO", "E30")
      		  } else { 
        			if (whlohead == "E10") {
        			  if ((state20 == "20") || (state20 == "50")) {
        				interactive.display.fields.put("OBWHLO", "E20")
        			  } else { 
        				interactive.display.fields.put("OBWHLO", "E10")
        			  }
        			} else { 
        			  if ((state10 == "20") || (state10 == "50")) {
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
    String stat = ""
    Map<String, String> params = ["CONO" : company, "WHLO" : pwhlo, "ITNO" : pitno]
    
    Closure<?> callback = {
      Map<String, String> response ->
      if (response.STAT != "") {
        stat = response.STAT
      }
    }
    miCaller.call("MMS200MI", "GetItmWhsBasic", params, callback)
    return stat
  }
}