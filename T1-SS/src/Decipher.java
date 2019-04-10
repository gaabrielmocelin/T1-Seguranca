import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Decipher {
	public static void main(String[] args) {
		String text = readFile("DemCifrado.txt");
		
		HashMap<String, Integer> possibleKeys = countTextRepetitionsOn(text);
		int keyLength = findKeyLenght(possibleKeys, text);
		System.out.println("Key Lenght: " + keyLength);
		
		String[] charactersGroupedByKey = groupCharsByKey(text, keyLength);
		String[] decryptedChars = decryptChars(charactersGroupedByKey);
		
		String decryptedText = getCharacteresTogether(decryptedChars, text, keyLength);
		System.out.println(decryptedText);
	}
	
	public static String readFile(String file) {
		BufferedReader br;
		String text = new String();
		String line;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null)  {
				text = text + line;
			} 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return text;
	}
	
	public static HashMap<String, Integer> countTextRepetitionsOn(String text) {
		// conta o numero de repeticoes de substrings de 3 em 3 char
		HashMap<String, Integer> possibleKeys = new HashMap<>(); 
	
		for(int i = 0; i<text.length()-2; i++) {
			String key = text.substring(i, i+3);
			if (possibleKeys.containsKey(key)) continue;
			
			int count = text.split(key, -1).length - 1;
			possibleKeys.put(key, count);
			//System.out.println(key+", "+count);
		}

		return possibleKeys;
	}
	
	public static int findKeyLenght(HashMap<String, Integer> possibleKeys, String text) {
		boolean foundKeyLength = false;
		int higherKeyCount = 0;
		String higherKey = new String();
		
		HashMap<Double, Integer> mdcs = new HashMap<>();
		int keyLength = 0;

		while(!foundKeyLength) {
			higherKey = "";
			higherKeyCount = 0;
			
			//Pega a substring que mais aparece
			for(String possibleKey : possibleKeys.keySet()) {
				if(higherKeyCount < possibleKeys.get(possibleKey)) {
					higherKeyCount = possibleKeys.get(possibleKey);
					higherKey = possibleKey;
				}
			}
			
			//remove do dicionario para n tentar posteriormente a mesma chave
			possibleKeys.remove(higherKey);
			
			//Calcula a distancia entre as repetições desta substring e adiciona em um array
			ArrayList<Integer> distanceBetweenKeys = new ArrayList<>();  
			String[] wordsBetweenKeys = text.split(higherKey);
			for(int i=0; i<wordsBetweenKeys.length; i++) {
				distanceBetweenKeys.add(wordsBetweenKeys[i].length()+higherKey.length());
			}
			
			//Calcula o mdc entre as distancias
			for(int i = 0; i < distanceBetweenKeys.size() - 1; i++) {
				double mdc = mdc(distanceBetweenKeys.get(i), distanceBetweenKeys.get(i+1));
				if (mdc>2) {
					if (mdcs.containsKey(mdc)) mdcs.put(mdc, mdcs.get(mdc)+1);
					else mdcs.put(mdc, 1);
				}
			}
			
			// se mais da metade dos mdcs forem o mesmo
			//achou o tamanho da chave
			int mdcCount = 0;
			for(int mdcValue : mdcs.values()) {
				mdcCount = mdcCount+mdcValue;
			}
			for(double mdc : mdcs.keySet()) {
				if (mdcs.get(mdc) >= mdcCount/2) {
					foundKeyLength = true;
					keyLength = (int) mdc;
				}
			}
		}
		
		return keyLength;
	}
	
	public static double mdc (double dividendo, double divisor){
		if ((dividendo % divisor == 0 )){
			return divisor;
		}else{
			return mdc(divisor,(dividendo % divisor));
		}
	}
	
	public static String[] groupCharsByKey(String text, int keyLength) {
		//concatena todos os caracteres correspondentes a cada posição da chave
		//ex: texts[0] == todos os caracteres q correspondem ao primero caractere da chave 
		
		String[] keyCharTexts = new String[keyLength];
		for(int i = 0; i<keyCharTexts.length; i++) keyCharTexts[i] = "";
		for(int i = 0; i<text.length(); i++) {
			int position = i % keyLength;
			keyCharTexts[position] = keyCharTexts[position] + text.charAt(i); 
		}
		
		return keyCharTexts;
	}
	
	public static String[] decryptChars(String[] keyCharTexts) {
		//acha o offset de cada caractere da chave e pega a letra correspondente para cada posicao
		for(int i = 0; i<keyCharTexts.length; i++) {
			String textCollumn = keyCharTexts[i];
			String newText = textCollumn;
			
			int offset = frequencyAnalysis(textCollumn);
			
			for(int j=0; j<textCollumn.length(); j++) {
				newText = getCorrespondingLetter(newText.charAt(j), j, newText, offset);
			}
			
			keyCharTexts[i] = newText;
		}
		
		return keyCharTexts;
	}
	
	static String alfabeto = "abcdefghijklmnopqrstuvwxyz"; 
	static HashMap<Character, Double> portugueseProbability = new HashMap<Character, Double>() {
		private static final long serialVersionUID = 1L;

		{
			put('a', 0.14634); put('b', 0.01043); put('c', 0.03882); put('d', 0.04992);
			put('e', 0.1257); put('f', 0.01023); put('g', 0.01303); put('h', 0.00781);
			put('i', 0.06186); put('j', 0.00397); put('k', 0.00015); put('l', 0.02779);
			put('m', 0.04738); put('n', 0.04446); put('o', 0.09735); put('p', 0.02523);
			put('q', 0.01204); put('r', 0.06530); put('s', 0.06805); put('t', 0.04336);
			put('u', 0.03639); put('v', 0.01575); put('w', 0.00037); put('x', 0.00253);
			put('y', 0.00006); put('z', 0.0047);
		}
	};
	
	public static int frequencyAnalysis(String text) {
		//conta o numero de aparições de cada char
		HashMap<Character, Integer> mostFrequentLetters = new HashMap<>();
		for(int i = 0; i<text.length(); i++) {
			if (mostFrequentLetters.containsKey(text.charAt(i))) {
				mostFrequentLetters.put(text.charAt(i), mostFrequentLetters.get(text.charAt(i))+1);
			} else {
				mostFrequentLetters.put(text.charAt(i), 1);
			}
		}
		
		HashMap<Character, Double> chiSquaredDictionary = new HashMap<>();
		
		//calcula chiSquared
		for(int i=0; i<alfabeto.length(); i++) {
			double chiSquared = 0;
			for(char letter: portugueseProbability.keySet()) {
				int newIndex = alfabeto.indexOf(letter) + i;
				if (newIndex>25) {
					newIndex = newIndex - 26;
				}
				
				if (mostFrequentLetters.containsKey(alfabeto.charAt(newIndex))) {
					int letterFrequency = mostFrequentLetters.get(alfabeto.charAt(newIndex));
					double mtFreq = portugueseProbability.get(letter) * text.length();
					chiSquared += Math.pow((letterFrequency - mtFreq), 2) / mtFreq;
				}
			}
			chiSquaredDictionary.put(alfabeto.charAt(i), chiSquared);
		}
		
		double lowerChiSquared = Double.POSITIVE_INFINITY;
		char keyLetter = ' ';
		
		//pega o menor chiSquared
		for (char letter: chiSquaredDictionary.keySet()) {
            if (chiSquaredDictionary.get(letter) < lowerChiSquared ) {
            	lowerChiSquared = chiSquaredDictionary.get(letter);
                keyLetter = letter;
            }
        }
		
		System.out.println(keyLetter);
		return alfabeto.indexOf(keyLetter);
	}
	
	public static String getCorrespondingLetter(char character, int characterIndex, String text, int offset) {
		int newIndex = alfabeto.indexOf(character)-offset;
		if (newIndex > 25) {
			newIndex = newIndex - 26;
		} else if (newIndex < 0) {
			newIndex = newIndex + 26; 
		}
		
		char newChar = alfabeto.charAt(newIndex);
		StringBuilder newText = new StringBuilder(text);
		newText.setCharAt(characterIndex, newChar);
		return newText.toString();
	}
	
	public static String getCharacteresTogether(String[] decryptedChars, String text, int keyLength) {
		//reagrupa os textos colocando os caracteres na ordem
		String decryptedText = "";
		for(int i = 0; i<text.length(); i++) {
			int textIndex = i % keyLength;
			int letterIndex = (int) i/keyLength;
			decryptedText = decryptedText + decryptedChars[textIndex].charAt(letterIndex);
		}
		
		return decryptedText;
	}
}
