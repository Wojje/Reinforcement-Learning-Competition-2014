Regler f�r monstervandring:

Monstervandringen sker �ver ett rutn�t om 6x6 rutor, numrerade fr�n 0 till 35. Ruta 0 finns uppe till v�nster, ruta 5 uppe till h�ver och ruta 35 nere till h�ger. (Interpolera resten!)

Actions
Man f�r ta ett av fyra m�jliga actions, numrerade fr�n 0 till 3
0: f�rs�k g� till v�nster
1: f�rs�k g� till h�ger
2: f�rs�k g� upp�t
3: f�rs�k g� ner�t
Det �r dock inte s�kert att ett action lyckas (v�r vandrare har f�tt i sig lite f�r mycket b�sk), s� med sannolikhet 15% g�r hen ist�llet i slumpm�ssigt vald riktning av ovanst�ende fyra m�jliga riktningar. 

Det finns en m�lruta, och om man tar sig till denna vinner man ett reward om 1.0 enheter. M�lrutan v�ljs slumpm�ssigt i b�rjan av varje episod. 

Det finns ett antal fail-rutor: 
1) dels �r rutorna p� kanten av 6x6-matrisen en avgrund (totalt 20 stycken avgrundsrutor allts�)
2) dels finns det tv� monster som i b�rjan av experimentet startar p� slumpm�ssigt valda rutor. Dessa monster r�r sig ibland i slumpm�ssig riktning till en n�rliggande ruta mellan episoderna. Ju fler episoder det g�tt sedan ett monster r�rde sig, desto mer sannolikt �r det att monstret r�r sig. St�rst sannolikhet att ett monster flyttar p� sig (30%) �r det efter att monstret st�tt still i minst 35 episoder. 

F�r varje steg som tas ges ett reward om -0.01 enheter. 

Vandringen startar p� slumpm�ssigt vald ruta som varken �r m�l- eller fail-ruta och slutar n�r vandraren n�r antingen en m�l- eller failruta. 