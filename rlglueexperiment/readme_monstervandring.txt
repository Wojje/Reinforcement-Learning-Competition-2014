Regler för monstervandring:

Monstervandringen sker över ett rutnät om 6x6 rutor, numrerade från 0 till 35. Ruta 0 finns uppe till vänster, ruta 5 uppe till höger och ruta 35 nere till höger. (Interpolera resten!)

Actions
Man får ta ett av fyra möjliga actions, numrerade från 0 till 3
0: försök gå till vänster
1: försök gå till höger
2: försök gå uppåt
3: försök gå neråt
Det är dock inte säkert att ett action lyckas (vår vandrare har fått i sig lite för mycket bäsk), så med sannolikhet 15% går hen istället i slumpmässigt vald riktning av ovanstående fyra möjliga riktningar. 

Det finns en målruta, och om man tar sig till denna vinner man ett reward om 1.0 enheter. Målrutan väljs slumpmässigt i början av varje episod. 

Det finns ett antal fail-rutor: 
1) dels är rutorna på kanten av 6x6-matrisen en avgrund (totalt 20 stycken avgrundsrutor alltså)
2) dels finns det två monster som i början av experimentet startar på slumpmässigt valda rutor. Dessa monster rör sig ibland i slumpmässig riktning till en närliggande ruta ett action. Ju fler actions det gått sedan ett monster rörde sig, desto mer sannolikt är det att monstret rör sig. Störst sannolikhet att ett monster flyttar på sig (30%) är det efter att monstret stått still i minst 20 actions. 

För varje steg som tas ges ett reward om -0.01 enheter. 

Vandringen startar på slumpmässigt vald ruta som varken är mål- eller fail-ruta och slutar när vandraren når antingen en mål- eller failruta. 