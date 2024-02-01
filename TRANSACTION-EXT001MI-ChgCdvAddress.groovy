/**
 * README
 * Création d'une API pour mettre à jour l'adresse d'une commande de vente. On bloquera la modification si le statut est >= 77 (facturé).
 *
 * Name: ChgCdvAddress
 * Description: 
 * Date       Changed By                     Description
 * 20240129   François Leprévost             Création verbe ChgCdvAddress
 */
public class ChgCdvAddress extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public ChgCdvAddress(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono = 0
  String chid = ""
  String cuno = ""
  Map<String, String> map = new HashMap<String, String>()
  String orno = ""
  String adidOOHEAD = ""
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    chid = program.getUser()
    
    orno = (mi.inData.get("ORNO") == null) ? "" : mi.inData.get("ORNO").trim()
    if (orno.isEmpty()) {
      mi.error("Le numéro de commande est obligatoire.")
      return
    } else {
      Optional<DBContainer> container = checkOrderExist(orno)
      if (container.isPresent()) {
        DBContainer record = container.get()
        int orsl = Integer.parseInt(String.valueOf(record.get("OAORSL")))
        int orst = Integer.parseInt(String.valueOf(record.get("OAORST")))
        if (orsl >= 77 || orst >= 77) {
          mi.error("La commande est facturée, l'adresse ne peut être modifiée.")
          return
        }
        int hocd = Integer.parseInt(String.valueOf(record.get("OAHOCD")))
        if (hocd > 0) {
          String message = "La commande est verrouillée par l'utilisateur " + record.get("OACHID") + ", la modification est impossible."
          mi.error(message)
          return
        }
        cuno = String.valueOf(record.get("OACUNO"))
        adidOOHEAD = String.valueOf(record.get("OAADID"))
      } else {
        mi.error("La commande est inexistante.")
        return
      }
    }
    
    String adrt = (mi.inData.get("ADRT") == null) ? "" : mi.inData.get("ADRT").trim()
    if (adrt.isEmpty()) {
      mi.error("Le type d'adresse est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", adrt, ".")) {
      mi.error("Le type de commande est incorrect.")
      return
    }
      
    String cscd = (mi.inData.get("CSCD") == null) ? "" : mi.inData.get("CSCD").trim()
    if (!cscd.isEmpty() && !checkCsytabExist("CSCD", cscd, "")) {
      mi.error("Le pays n'existe pas")
      return
    }
    
    String modl = (mi.inData.get("MODL") == null) ? "" : mi.inData.get("MODL").trim()
    if (!modl.isEmpty() && !checkCsytabExist("MODL", modl, "FR")) {
      if (!checkCsytabExist("MODL", modl, "FR")) {
        mi.error("Le mode de livraison n'existe pas")
        return
      }
    }
    
    String tedl = (mi.inData.get("TEDL") == null) ? "" : mi.inData.get("TEDL").trim()
    if (!tedl.isEmpty() && !checkCsytabExist("TEDL", tedl, "FR")) {
      if (!checkCsytabExist("TEDL", tedl, "")) {
        mi.error("Les conditions de livraison n'existent pas")
        return
      }
    }
    
    String adid = (mi.inData.get("ADID") == null) ? "" : mi.inData.get("ADID").trim()
    if (adid.isEmpty() && adrt.equals("1")) {
        mi.error("Le code adresse est obligatoire pour l'adresse de livraison (ADRT = 1)")
        return
    }
    
    alimMap(cscd)
    updateAddress(orno, adrt)
  }
  
  /**
  * On enregistre dans la HashMap les données en entrée de l'API.
  */
  private alimMap(String cscd) {
    String adid = (mi.inData.get("ADID") == null) ? "" : mi.inData.get("ADID").trim()
    String cunm = (mi.inData.get("CUNM") == null) ? "" : mi.inData.get("CUNM").trim()
    String cua1 = (mi.inData.get("CUA1") == null) ? "" : mi.inData.get("CUA1").trim()
    String cua2 = (mi.inData.get("CUA2") == null) ? "" : mi.inData.get("CUA2").trim()
    String cua3 = (mi.inData.get("CUA3") == null) ? "" : mi.inData.get("CUA3").trim()
    String pono = (mi.inData.get("PONO") == null) ? "" : mi.inData.get("PONO").trim()
    String town = (mi.inData.get("TOWN") == null) ? "" : mi.inData.get("TOWN").trim()
    String cua4 = pono + " " + cua1
    String ecar = (mi.inData.get("ECAR") == null) ? "" : mi.inData.get("ECAR").trim()
    String phno = (mi.inData.get("PHNO") == null) ? "" : mi.inData.get("PHNO").trim()
    String tfno = (mi.inData.get("TFNO") == null) ? "" : mi.inData.get("TFNO").trim()
    String yref = (mi.inData.get("YREF") == null) ? "" : mi.inData.get("YREF").trim()
    String vrno = (mi.inData.get("VRNO") == null) ? "" : mi.inData.get("VRNO").trim()
    String sple = (mi.inData.get("SPLE") == null) ? "" : mi.inData.get("SPLE").trim()
    String hafe = (mi.inData.get("HAFE") == null) ? "" : mi.inData.get("HAFE").trim()
    String rasn = (mi.inData.get("RASN") == null) ? "" : mi.inData.get("RASN").trim()
    String edes = (mi.inData.get("EDES") == null) ? "" : mi.inData.get("EDES").trim()
    String dlsp = (mi.inData.get("DLSP") == null) ? "" : mi.inData.get("DLSP").trim()
    String dstx = (mi.inData.get("DSTX") == null) ? "" : mi.inData.get("DSTX").trim()
    String modl = (mi.inData.get("MODL") == null) ? "" : mi.inData.get("MODL").trim()
    String tedl = (mi.inData.get("TEDL") == null) ? "" : mi.inData.get("TEDL").trim()
    String tel2 = (mi.inData.get("TEL2") == null) ? "" : mi.inData.get("TEL2").trim()
    
    map.put("adid", adid)
    map.put("cunm", cunm)
    map.put("cua1", cua1)
    map.put("cua2", cua2)
    map.put("cua3", cua3)
    map.put("pono", pono)
    map.put("town", town)
    map.put("cua4", cua4.trim())
    map.put("ecar", ecar)
    map.put("phno", phno)
    map.put("tfno", tfno)
    map.put("yref", yref)
    map.put("vrno", vrno)
    map.put("sple", sple)
    map.put("cscd", cscd)
    map.put("hafe", hafe)
    map.put("rasn", rasn)
    map.put("edes", edes)
    map.put("dlsp", dlsp)
    map.put("dstx", dstx)
    map.put("modl", modl)
    map.put("tedl", tedl)
    map.put("tel2", tel2)
  }
  
  /**
  * On vérifie que le numéro de commande existe et on récupère les certaines données de la table.
  */
  private Optional<DBContainer> checkOrderExist(String orno) {
    DBAction query = database.table("OOHEAD")
      .index("00")
      .selectAllFields()
      .build()
    DBContainer container = query.getContainer()
    container.set("OACONO", cono)
    container.set("OAORNO", orno)
    
    boolean found = query.read(container)
    
    if (found) {
      return Optional.of(container)
    } else {
      return Optional.empty()
    }
  }
  
  /**
  * On vérifie que les enregistrements de CSYTAB existent.
  */
  private boolean checkCsytabExist(String stco, String stky, String lncd) {
    DBAction query = database.table("CSYTAB")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("CTCONO", cono)
    container.set("CTDIVI", "")
    container.set("CTSTCO", stco)
    container.set("CTSTKY", stky)
    container.set("CTLNCD", lncd)
    
    return query.read(container)
  }
  
  /**
  * Modification de l'adresse, si on ne la trouve pas on la crée.
  */
  private void updateAddress(String orno, String adrt) {
    DBAction query = database.table("OOADRE")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("ODCONO", cono)
    container.set("ODORNO", orno)
    container.set("ODADRT", utility.call("NumberUtil","parseStringToInteger", adrt))
    container.set("ODADID", map.get("adid"))
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->
      if (!map.get("cunm").isEmpty()) {
        lockedResult.set("ODCUNM", map.get("cunm"))
      }
      if (!map.get("cua1").isEmpty()) {
        lockedResult.set("ODCUA1", map.get("cua1"))
      }
      if (!map.get("cua2").isEmpty()) {
        lockedResult.set("ODCUA2", map.get("cua2"))
      }
      if (!map.get("cua3").isEmpty()) {
        lockedResult.set("ODCUA3", map.get("cua3"))
      }
      if (!map.get("cua4").isEmpty()) {
        lockedResult.set("ODCUA4", map.get("cua4"))
      }
      if (!map.get("pono").isEmpty()) {
        lockedResult.set("ODPONO", map.get("pono"))
      }
      if (!map.get("town").isEmpty()) {
        lockedResult.set("ODTOWN", map.get("town"))
      }
      if (!map.get("ecar").isEmpty()) {
        lockedResult.set("ODECAR", map.get("ecar"))
      }
      if (!map.get("cscd").isEmpty()) {
        lockedResult.set("ODCSCD", map.get("cscd"))
      }
      if (!map.get("phno").isEmpty()) {
        lockedResult.set("ODPHNO", map.get("phno"))
      }
      if (!map.get("tfno").isEmpty()) {
        lockedResult.set("ODTFNO", map.get("tfno"))
      }
      if (!map.get("yref").isEmpty()) {
        lockedResult.set("ODYREF", map.get("yref"))
      }
      if (!map.get("vrno").isEmpty()) {
        lockedResult.set("ODVRNO", map.get("vrno"))
      }
      if (!map.get("sple").isEmpty()) {
        lockedResult.set("ODSPLE", map.get("sple"))
      }
      if (!map.get("cscd").isEmpty()) {
        lockedResult.set("ODCSCD", map.get("cscd"))
      }
      if (!map.get("hafe").isEmpty()) {
        lockedResult.set("ODHAFE", map.get("hafe"))
      }
      if (!map.get("rasn").isEmpty()) {
        lockedResult.set("ODRASN", map.get("rasn"))
      }      
      if (!map.get("edes").isEmpty()) {
        lockedResult.set("ODEDES", map.get("edes"))
      }
      if (!map.get("dlsp").isEmpty()) {
        lockedResult.set("ODDLSP", map.get("dlsp"))
      }     
      if (!map.get("dstx").isEmpty()) {
        lockedResult.set("ODDSTX", map.get("dstx"))
      }
      if (!map.get("modl").isEmpty()) {
        lockedResult.set("ODMODL", map.get("modl"))
      }
      if (!map.get("tedl").isEmpty()) {
        lockedResult.set("ODTEDL", map.get("tedl"))
      }
      if (!map.get("tel2").isEmpty()) {
        lockedResult.set("ODTEL2", map.get("tel2"))
      }
      int chno = lockedResult.get("ODCHNO")
      chno++
      lockedResult.set("ODLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      lockedResult.set("ODCHNO", chno)
      lockedResult.set("ODCHID", chid)
      
      lockedResult.update()
    }
    
    if (!query.readLock(container, updateCallBack)) {
      createAddress(orno, adrt)
    }
    
    if (adrt.equals("1")) {
      updatAdidInOOHEAD()
    }
  }
  
  /**
  * Création de l'adresse dans OOADRE.
  */
  private void createAddress(String orno, String adrt) {
     DBAction query = database.table("OOADRE")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("ODCONO", cono)
    container.set("ODORNO", orno)
    container.set("ODADRT", utility.call("NumberUtil","parseStringToInteger", adrt))
    container.set("ODADID", map.get("adid"))
    
    Optional<DBContainer> customerContainer = searchCustomerAddress(adrt)
    boolean cidmasExist = false
    DBContainer record = null
    if (customerContainer.isPresent()) {
      cidmasExist = true
      record = customerContainer.get()
    }
    
    if (!map.get("cunm").isEmpty()) {
      container.set("ODCUNM", map.get("cunm"))
    } else if (cidmasExist) {
      container.set("ODCUNM", String.valueOf(record.get("OPCUNM")))
    }
    if (!map.get("cua1").isEmpty()) {
      container.set("ODCUA1", map.get("cua1"))
    } else if (cidmasExist) {
      container.set("ODCUA1", String.valueOf(record.get("OPCUA1")))
    }
    if (!map.get("cua2").isEmpty()) {
      container.set("ODCUA2", map.get("cua2"))
    } else if (cidmasExist) {
      container.set("ODCUA2", String.valueOf(record.get("OPCUA2")))
    }
    if (!map.get("cua3").isEmpty()) {
      container.set("ODCUA3", map.get("cua3"))
    } else if (cidmasExist) {
      container.set("ODCUA3", String.valueOf(record.get("OPCUA3")))
    }
    if (!map.get("cua4").isEmpty()) {
      container.set("ODCUA4", map.get("cua4"))
    } else if (cidmasExist) {
      container.set("ODCUA4", String.valueOf(record.get("OPCUA4")))
    }
    if (!map.get("pono").isEmpty()) {
      container.set("ODPONO", map.get("pono"))
    } else if (cidmasExist) {
      container.set("ODPONO", String.valueOf(record.get("OPPONO")))
    }
    if (!map.get("town").isEmpty()) {
      container.set("ODTOWN", map.get("town"))
    } else if (cidmasExist) {
      container.set("ODTOWN", String.valueOf(record.get("OPTOWN")))
    }
    if (!map.get("ecar").isEmpty()) {
      container.set("ECAR", map.get("ecar"))
    } else if (cidmasExist) {
      container.set("ODECAR", String.valueOf(record.get("OPECAR")))
    }
    if (!map.get("cscd").isEmpty()) {
      container.set("ODCSCD", map.get("cscd"))
    } else if (cidmasExist) {
      container.set("ODCSCD", String.valueOf(record.get("OPCSCD")))
    }
    if (!map.get("phno").isEmpty()) {
      container.set("ODPHNO", map.get("phno"))
    } else if (cidmasExist) {
      container.set("ODPHNO", String.valueOf(record.get("OPPHNO")))
    }
    if (!map.get("tfno").isEmpty()) {
      container.set("ODTFNO", map.get("tfno"))
    } else if (cidmasExist) {
      container.set("ODTFNO", String.valueOf(record.get("OPTFNO")))
    }
    if (!map.get("yref").isEmpty()) {
      container.set("ODYREF", map.get("yref"))
    } else if (cidmasExist) {
      container.set("ODYREF", String.valueOf(record.get("OPYREF")))
    }
    if (!map.get("vrno").isEmpty()) {
      container.set("ODVRNO", map.get("vrno"))
    } else if (cidmasExist) {
      container.set("ODVRNO", String.valueOf(record.get("OPVRNO")))
    }
    if (!map.get("sple").isEmpty()) {
      container.set("ODSPLE", map.get("sple"))
    } else if (cidmasExist) {
      container.set("ODSPLE", String.valueOf(record.get("OPSPLE")))
    }
    if (!map.get("hafe").isEmpty()) {
      container.set("ODHAFE", map.get("hafe"))
    } else if (cidmasExist) {
      container.set("ODHAFE", String.valueOf(record.get("OPHAFE")))
    }
    if (!map.get("rasn").isEmpty()) {
      container.set("ODRASN", map.get("hafe"))
    } else if (cidmasExist) {
      container.set("ODRASN", String.valueOf(record.get("OPRASN")))
    }    
    if (!map.get("edes").isEmpty()) {
      container.set("ODEDES", map.get("edes"))
    } else if (cidmasExist) {
      container.set("ODEDES", String.valueOf(record.get("OPEDES")))
    }    
    if (!map.get("dlsp").isEmpty()) {
      container.set("ODDLSP", map.get("dlsp"))
    }  
    if (!map.get("dstx").isEmpty()) {
      container.set("ODDSTX", map.get("dstx"))
    }   
    if (!map.get("modl").isEmpty()) {
      container.set("ODMODL", map.get("modl"))
    } else if (cidmasExist) {
      container.set("ODMODL", String.valueOf(record.get("OPMODL")))
    }    
    if (!map.get("tedl").isEmpty()) {
      container.set("ODTEDL", map.get("tel"))
    } else if (cidmasExist) {
      container.set("ODTEDL", String.valueOf(record.get("OPTEDL")))
    } 
    if (!map.get("tel2").isEmpty()) {
      container.set("ODTEL2", map.get("tel2"))
    } else if (cidmasExist) {
      container.set("ODTEL2", String.valueOf(record.get("OPTEL2")))
    }  
    
    container.set("ODRGDT", utility.call("DateUtil","currentDateY8AsInt"))
    container.set("ODRGTM", utility.call("DateUtil","currentTimeAsInt"))
    container.set("ODLMDT", utility.call("DateUtil","currentDateY8AsInt"))
    container.set("ODCHNO", 1)
    container.set("ODCHID", chid)
    
    query.insert(container)
  }
  
  /**
  * Recherche de l'adresse client pour récupérer les données de ladresse client lors d'une création dans OOADRE.
  */
  private Optional<DBContainer> searchCustomerAddress(String adrt) {
    DBAction query = database.table("OCUSAD")
    .index("00")
    .selectAllFields()
    .build()
    DBContainer container = query.getContainer()
    
    container.set("OPCONO", cono)
    container.set("OPCUNO", cuno)
    container.set("OPADRT", utility.call("NumberUtil","parseStringToInteger", adrt))
    container.set("OPADID", map.get("adid"))
    
    boolean found = query.read(container)
    
    if (found) {
      return Optional.of(container)
    } else {
      return Optional.empty()
    }
  }
  
  /**
  * Mise à jour de l'ADID dans OOHEAD s'il est différent de celui rentré en entrée de l'API.
  */
  private void updatAdidInOOHEAD() {
    if (adidOOHEAD.isEmpty() || !adidOOHEAD.equals(map.get("adid"))) {
      adidOOHEAD = map.get("adid")
      
      DBAction query = database.table("OOHEAD")
        .index("00")
        .build()
      DBContainer container = query.getContainer()
      container.set("OACONO", cono)
      container.set("OAORNO", orno)
      
      Closure<?> updateCallBack = { LockedResult lockedResult ->
        lockedResult.set("OAADID", adidOOHEAD)
        int chno = lockedResult.get("OACHNO")
        chno++
        lockedResult.set("OALMDT", utility.call("DateUtil","currentDateY8AsInt"))
        lockedResult.set("OACHNO", chno)
        lockedResult.set("OACHID", chid)
  
        lockedResult.update()
      }
      
      query.readLock(container, updateCallBack)
    }
  }

}