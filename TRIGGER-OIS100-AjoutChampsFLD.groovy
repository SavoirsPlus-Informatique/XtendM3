/**
 * README
 * Initialisation des champs de l'écran OIS100/I WWFLD6, WWFLD7 et WWFLD8 
 * avec les champs OAUCA0, OAUCA1 et OAUCA2 de la commande
 *
 * Name: AjoutChampsFLD
 * Description: 
 * Date       Changed By                     Description
 * 20231117   Ludovic Travers                Création de la classe AjoutChampsFLD
 */
public class AjoutChampsFLD extends ExtendM3Trigger {
  
  private final ProgramAPI program
  private final InteractiveAPI interactive
  
  public AjoutChampsFLD(ProgramAPI program, InteractiveAPI interactive) {
    this.program = program
    this.interactive = interactive
  }
  
  public void main() {
    def OHEAD = program.getTableRecord("OOHEAD")
    int CONO = OHEAD.OACONO
    String ORNO = OHEAD.OAORNO
    String UCA0 = OHEAD.OAUCA0
    String UCA1 = OHEAD.OAUCA1
    String UCT1 = OHEAD.OAUCT1
   
    interactive.display.fields.put("WWFLD6", UCA0)
    interactive.display.fields.put("WWFLD7", UCA1)
    interactive.display.fields.put("WWFLD8", UCT1)
  }
}