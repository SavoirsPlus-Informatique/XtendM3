/**
 * README
 * Mise à jour des champs OAUCA0, OAUCA1 et OAUCA2 de la commande
 * avec les champsWWFLD6, WWFLD7 et WWFLD8 de l'écran OIS100/I 
 *
 * Name: MajChampsFLD
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création de la classe MajChampsFLD
 */
public class MajChampsFLD extends ExtendM3Trigger {
  
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  
  public MajChampsFLD(ProgramAPI program, InteractiveAPI interactive,
                                DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.database = database
  }
  
  public void main() {
    
    def OHEAD = program.getTableRecord("OOHEAD")
    int CONO = OHEAD.OACONO
    String ORNO = OHEAD.OAORNO
   
    DBAction query = database.table("OOHEAD")
      .index("00")
      .build()
      
    DBContainer container = query.getContainer()
    
    container.set("OACONO", CONO)
    container.set("OAORNO", ORNO)
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->
   
      lockedResult.set("OAUCA0", interactive.display.fields.WWFLD6)
      lockedResult.set("OAUCA1", interactive.display.fields.WWFLD7)
      lockedResult.set("OAUCT1", interactive.display.fields.WWFLD8)
    
      lockedResult.update()
    }
    
    if (query.read(container)) {
      query.readLock(container, updateCallBack)
    } 
  }
}