/**
 * README
 * Mise à jour du flag EGTGCD à la valeur 9 dans la table FGLEDG.
 * Cette API sera utilisé dans les interfaces MEC LDCompta
 *
 * Name: SetTGCD9
 * Description: 
 * Date       Changed By                     Description
 * 20230808   François Leprévost             Création verbe SetTGCD9 sur table FGLEDG
 */
public class SetTGCD9 extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public SetTGCD9(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono = 0
  String chid = ""
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    chid = program.getUser()
    String divi = mi.inData.get("DIVI").trim()
    String yea4 = mi.inData.get("YEA4").trim()
    String jrno = mi.inData.get("JRNO").trim()
    String jsno = mi.inData.get("JSNO").trim()

    if (divi.isEmpty()) {
      mi.error("La division est obligatoire.")
      return
    } else if (!checkDiviExist(divi)) {
      mi.error("La division est inexistante.")
      return
    }
    
    if (yea4.isEmpty()) {
      mi.error("L'année est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", yea4, ".")) {
      mi.error("L'année est incorrecte.")
      return
    }
    
    if (jrno.isEmpty()) {
      mi.error("Le numéro de journal est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", jrno, ".")) {
      mi.error("Le numéro de journal est incorrect.")
      return
    }
    
    if (jsno.isEmpty()) {
      mi.error("Le numéro de séquence journal est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", jsno, ".")) {
      mi.error("Le numéro de séquence journal est incorrect.")
      return
    }
    
    updateFGLEDG(divi, yea4, jrno, jsno)
  }
  
  /**
  * On vérifie que la DIVI existe
  */
  private boolean checkDiviExist(String divi) {
    DBAction query = database.table("CMNDIV")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("CCCONO", cono)
    container.set("CCDIVI", divi)
    
    return query.read(container)
  }
  
  /**
  * Mise à jour de l'enregistrement
  */
  private void updateFGLEDG(String divi, String yea4, String jrno, String jsno) {
    DBAction query = database.table("FGLEDG")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    
    container.set("EGCONO", cono)
    container.set("EGDIVI", divi)
    container.set("EGYEA4", utility.call("NumberUtil","parseStringToInteger", yea4))
    container.set("EGJRNO", utility.call("NumberUtil","parseStringToInteger", jrno))
    container.set("EGJSNO", utility.call("NumberUtil","parseStringToInteger", jsno))
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->
      int chno = lockedResult.get("EGCHNO")
      chno++
      lockedResult.set("EGLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      lockedResult.set("EGCHNO", chno)
      lockedResult.set("EGCHID", chid)
      lockedResult.set("EGTGCD", 9)
      
      lockedResult.update()
    }
    
    if (!query.readLock(container, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas.")
    }
  }
}
