
public class TravailGeo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String geom="POLYGON ((50.632311503003585 3.030104756841387, 50.63255989741075 3.030747049655475, 50.63279180287725 3.031352933874899, 50.632976357321056 3.031181184675537, 50.63298521826647 3.03120363979428, 50.63303120874586 3.031321264397611, 50.63302388521822 3.031358864026043, 50.63296283908466 3.031406603638286, 50.632954813778554 3.031380212221841, 50.632911836687455 3.031412808522343, 50.632920660730036 3.031439073428172, 50.63289229655721 3.031460588074164, 50.63278684340402 3.031552436676252, 50.632385121456736 3.031857416414753, 50.63235506070128 3.031880284097809, 50.632189013129434 3.032005519224683, 50.63216745276009 3.031986710762197, 50.632146632684794 3.031950787179946, 50.63213374689206 3.031910889609426, 50.63212140400491 3.031853608712742, 50.63203626776923 3.031455226167433, 50.631982496274794 3.031196512027782, 50.63196401596988 3.031205418128083, 50.63193596786005 3.031219129814743, 50.631902938387576 3.031235335871902, 50.63190491138255 3.031240698928672, 50.63190328723528 3.031275549810079, 50.631892147980444 3.031319707406271, 50.63186746184131 3.031346783341005, 50.631877929700465 3.031402637533748, 50.63189670462483 3.031399855478329, 50.631932776098786 3.031420337655696, 50.63195719749322 3.031481929379414, 50.6319576987653 3.031487009327093, 50.63203628317873 3.031852369474212, 50.632064204851496 3.031981678239087, 50.63206886246985 3.032016646092858, 50.63206710745483 3.032070898399649, 50.63206196585414 3.032103037974787, 50.63191016266531 3.032218728251488, 50.631902614583346 3.032221432544048, 50.631875699493186 3.032226001048504, 50.63183587513792 3.032212288737275, 50.6318108859467 3.032196032039663, 50.631778713064136 3.032161554690157, 50.6316019964282 3.031932845526045, 50.63102321196951 3.031139547382118, 50.63073518316484 3.030809595733271, 50.63073452541539 3.03078400024468, 50.630240259915254 3.03022116123458, 50.630227045308075 3.030202429834936, 50.630218050323094 3.030176716727071, 50.630205392143644 3.030157872804726, 50.63019740965744 3.030138707176993, 50.63020455299374 3.030102619106116, 50.63020882142884 3.030079369041256, 50.63035367933201 3.029654487485995, 50.63047818650278 3.029286707609573, 50.6304859637286 3.029263501722282, 50.63055281485536 3.029066174217892, 50.63056412959021 3.029035816689134, 50.63058072284929 3.028998449671389, 50.63059362436682 3.028975176099926, 50.63060759306233 3.028955162475505, 50.63070227540031 3.028824955942897, 50.630844904538485 3.028629010628765, 50.630871402733426 3.028602795638774, 50.63090206006716 3.0285980016582, 50.630926538617885 3.028612562803766, 50.630951172574214 3.028653692824233, 50.63141440832719 3.029796424859852, 50.63145937856331 3.029758904955939, 50.631487635279406 3.0297354286147, 50.63174246269794 3.03035643502657, 50.631843346701274 3.030596197717217, 50.631867929209584 3.030551963521318, 50.632158151750325 3.030246754118372, 50.632311503003585 3.030104756841387, 50.632311503003585 3.030104756841387))";
		//System.out.println( doPOLYGON(geom)  );
		
		String grom2="MULTIPOLYGON (((50.624644383790184 3.039798619967061, 50.62458021072452 3.039873807197373, 50.62457714942954 3.039877007287829, 50.62457206403771 3.03986739568971, 50.624570026335114 3.039869002386464, 50.624539497553364 3.039806550211678, 50.62457617280872 3.039760149782645, 50.62457820845822 3.039764948121002, 50.62458533719491 3.039755360309204, 50.62458330103454 3.03975215617664, 50.624618953187046 3.039705754858697, 50.62462098883401 3.039710567306964, 50.62462811757055 3.039700965371252, 50.62462609038328 3.039697761245257, 50.624629142699035 3.039694561133664, 50.62462201266661 3.039680151197484, 50.62461896035121 3.039683351309497, 50.62459250278326 3.039630509962882, 50.62459963151391 3.039620908021254, 50.6245965817531 3.039616108886863, 50.624603723035115 3.039595291070254, 50.62460778740653 3.039598510904729, 50.62461186688544 3.039582501506709, 50.624607793026435 3.039580889981083, 50.62460780016806 3.039558486436844, 50.62461085196779 3.039556894632941, 50.62460882987804 3.039537677904769, 50.62460475550616 3.039537674697669, 50.62459152694981 3.039510449906058, 50.624592542582285 3.03950565397659, 50.62459560386761 3.039502453862519, 50.62459356770075 3.039499249739552, 50.624591529495675 3.039502450658277, 50.624582360216856 3.039494444198885, 50.62458032997169 3.039500833536133, 50.62456302194165 3.039489618160059, 50.6245630239791 3.03948321312226, 50.62455182292415 3.039486406836605, 50.62455182139373 3.039491217667887, 50.624535529826694 3.039500798305068, 50.624533485192465 3.039495999974438, 50.624527380058424 3.039504008521489, 50.62449888675318 3.039446354939432, 50.624500924957076 3.039443154024333, 50.624495840037525 3.039431948281413, 50.62449277875803 3.039435134285432, 50.62449074309553 3.039430335968603, 50.62448157668116 3.039441544627728, 50.624483612854334 3.039444734633222, 50.624450000016225 3.03948473258968, 50.62444897795447 3.039481529273825, 50.624439811539396 3.039492723806311, 50.62443065479493 3.039473515647312, 50.62439094464907 3.039468673623666, 50.62437974053624 3.039481474851952, 50.62437261047647 3.039467065017568, 50.62436955765851 3.039471859325944, 50.62436752250839 3.0394654527122, 50.62435122737166 3.039486249145447, 50.624353263539604 3.039489453252429, 50.62431149785788 3.03954224056584, 50.624309461686686 3.039539050567325, 50.62429315807439 3.039558238640774, 50.624296216813164 3.039563037749732, 50.62429010269827 3.039571046243736, 50.62429824634468 3.039587051065452, 50.62428500400951 3.03960305313882, 50.624279895596636 3.039665476731466, 50.62429006641967 3.039684685669014, 50.62427988536355 3.039697487631967, 50.62428089844559 3.039700690934338, 50.624249322701964 3.039740690169866, 50.62424728654473 3.039737471951406, 50.624239134720646 3.039747072999571, 50.62424117036715 3.039751885413124, 50.62423811804818 3.039755085496514, 50.6242442165361 3.039767886212827, 50.62424626372345 3.039764685334109, 50.624275770481745 3.039823947915024, 50.624271694568904 3.039828741381235, 50.62427373021214 3.039833553801217, 50.62425132139845 3.03986073611718, 50.62424928523979 3.039857532001651, 50.62423909519083 3.039870333912051, 50.624241131349194 3.039873538027451, 50.62423604184004 3.039876736486156, 50.62418210424991 3.039769445339212, 50.62417701935741 3.039758239634386, 50.62417396114069 3.039751832223935, 50.62416785906839 3.039750219093571, 50.62416174648105 3.039753416750413, 50.62415869416169 3.03975661682865, 50.62415461876139 3.039759816096674, 50.62414953028035 3.039759812068863, 50.62414443487627 3.03975340304954, 50.62410780803428 3.039676542501518, 50.624103739285886 3.039658932644257, 50.62410374490986 3.039641326005324, 50.62410782490622 3.039623722580436, 50.624113931073786 3.039612511624564, 50.624305438706294 3.039377370362923, 50.624316642809625 3.039364569130351, 50.62432887049741 3.039350174495569, 50.624505097110955 3.039134220316273, 50.62457232253686 3.03905423790098, 50.62458556529703 3.039036641385228, 50.624655852427686 3.038951850243407, 50.62469047918513 3.03891186666009, 50.624704744503305 3.038895865067561, 50.624713909358285 3.038889467105878, 50.624721036024006 3.038886270093843, 50.62472613347517 3.038886274040153, 50.62473428071471 3.038891077089407, 50.62479534978084 3.038953552634008, 50.62503354357123 3.039195452264376, 50.625041687252676 3.039211471370232, 50.62504168270172 3.039225875788562, 50.62503963995204 3.039243481164681, 50.625032510746614 3.039254691572479, 50.625022319254114 3.039272290573011, 50.62497240531642 3.039352286975862, 50.62492248234915 3.039432283212679, 50.62487255932664 3.039512279290479, 50.6248572772796 3.039536279253447, 50.62484607215161 3.039552269014355, 50.62482365984171 3.039590667693884, 50.624772722604334 3.039670662655949, 50.62471872863265 3.039739453237083, 50.624654562529685 3.039821045710841, 50.624644383790184 3.039798619967061)), ((50.624500752891564 3.039980993128618, 50.62443046349354 3.040070579280187, 50.62440091636953 3.040108971713648, 50.62438768604871 3.040115366166174, 50.62437444882298 3.040115355594437, 50.62435714032815 3.040105734251485, 50.62434389929992 3.04008971115055, 50.62429811446564 3.039998438547978, 50.62430116679061 3.039995238475552, 50.62430320294555 3.039998442599974, 50.62431236941262 3.039987234082279, 50.62430931914666 3.039984043258419, 50.62433478980576 3.039952038482034, 50.624336825444026 3.039956850913056, 50.62434089290468 3.039950449137549, 50.624368372908435 3.040004899509998, 50.624366335208 3.0400064920867, 50.624372433664306 3.040019306974171, 50.62437651907671 3.040012905213479, 50.62437956882316 3.040017704350939, 50.62438669758145 3.040008116612928, 50.62438363886482 3.040003303360261, 50.62441726187416 3.039960117409183, 50.624419297516404 3.039964915740463, 50.624426426274724 3.039955313879404, 50.624424390119046 3.039952109748871, 50.624460041837594 3.039907317029805, 50.624490571085545 3.039968174963907, 50.62448853286808 3.039971375857042, 50.62449362671338 3.039982581660083, 50.624496679550504 3.039977787370907, 50.62449871519184 3.039982585710489, 50.624500752891564 3.039980993128618)))";
		System.out.println( doMULTIPOLYGON(grom2)  );
	
	}
	
	public static String geoShape2SRID(String in) {
		if (in.contains("MULTIPOLYGON")) {
			return doMULTIPOLYGON(in);
		} else if (in.contains("POLYGON")) {
			return doPOLYGON(in);
		} else {
			return null;
		}
	}
	public static String doMULTIPOLYGON(String geoshape) {
		StringBuilder ret=new StringBuilder();
		ret.append("MULTIPOLYGON (((");
		String[] base = geoshape.split("MULTIPOLYGON \\(\\(\\(");
		String[] base2 = base[1].split("\\)\\)\\)");
		
		System.out.println(base2.length);
		String polys = base2[0];
		
		String[] tpoints = polys.split("\\)\\), \\(\\(");
		for (int i = 0; i < tpoints.length; i++) {
			if (i>0) {
				ret.append(")), ((");
			}
			ret.append(performPoints(tpoints[i]));
		}
		
		ret.append(")))");
		return ret.toString();
	
	}

	public static String doPOLYGON(String geoshape) {
		StringBuilder ret=new StringBuilder();
		ret.append("POLYGON ((");
		String[] base = geoshape.split("POLYGON \\(\\(");
		String[] base2 = base[1].split("\\)\\)");
		
		System.out.println(base2.length);
		String data = base2[0];
		
		ret.append(performPoints(data));
		
		ret.append("))");
		return ret.toString();
	}
	
	private static StringBuilder performPoints(String data) {
		StringBuilder ret=new StringBuilder();
		String[] work = data.split(", ");
		for (int i = 0; i < work.length; i++) {
			String[] point = work[i].split(" ");
			if (i>0) {
				ret.append(", ");
			}
			ret.append(point[1]).append(" ").append(point[0]);
		}
		return ret;
	}
}