import java.io.*;
import java.util.*;

public class Pass1 
{
    static Map<String, String> OPTAB = Map.ofEntries(
        Map.entry("STOP","IS,00"), Map.entry("ADD","IS,01"), Map.entry("SUB","IS,02"),
        Map.entry("MULT","IS,03"), Map.entry("MOVER","IS,04"), Map.entry("MOVEM","IS,05"),
        Map.entry("COMP","IS,06"), Map.entry("BC","IS,07"), Map.entry("DIV","IS,08"),
        Map.entry("READ","IS,09"), Map.entry("PRINT","IS,10"),
        Map.entry("START","AD,01"), Map.entry("END","AD,02"), Map.entry("ORIGIN","AD,03"),
        Map.entry("EQU","AD,04"), Map.entry("LTORG","AD,05"), Map.entry("DC","DL,01"),
        Map.entry("DS","DL,02")
    );
    static Map<String, String> REGTAB = Map.of("AREG","01","BREG","02","CREG","03","DREG","04");
    static Map<String, String> CCTAB = Map.of("LT","01","LE","02","EQ","03","GT","04","GE","05","NE","06","ANY","07");

    static LinkedHashMap<String,Integer> SYMTAB = new LinkedHashMap<>();
    static LinkedHashMap<String,String> SYMNAME = new LinkedHashMap<>();
    static Map<String,Integer> LITADDR = new LinkedHashMap<>();
    static List<String> LITS = new ArrayList<>(), IC = new ArrayList<>();
    static List<Integer> POOLTAB = new ArrayList<>();
    static int LC = 0, symCount = 0, nextLit = 0;

    public static void main(String[] a) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.split(";|//")[0].trim();
                if (s.isEmpty()) continue;
                String[] t = s.split("[\\s,]+");
                String label = null, op = t[0].toUpperCase();
                int i = 1;
                if (!OPTAB.containsKey(op)) { label = op; op = t[i++].toUpperCase(); }

                switch (op) {
                    case "START" -> {
                        LC = t.length > i ? Integer.parseInt(t[i]) : 0;
                        IC.add(String.format("%-4s (%s) (C,%d)", "", OPTAB.get(op), LC));
                    }
                    case "END" -> { closeLiterals(); IC.add(String.format("%-4d (%s)", LC, OPTAB.get(op))); }
                    case "LTORG" -> { IC.add(String.format("%-4d (%s)", LC, OPTAB.get(op))); closeLiterals(); }
                    case "ORIGIN" -> { LC = eval(t[i]); IC.add(String.format("%-4s (%s) (C,%d)", "", OPTAB.get(op), LC)); }
                    case "EQU" -> { if (label != null) define(label, eval(t[i])); IC.add(String.format("%-4s (%s)", "", OPTAB.get(op))); }
                    case "DS" -> { define(label, LC); int n=eval(t[i]); IC.add(String.format("%-4d (%s) (C,%d)", LC, OPTAB.get(op), n)); LC+=n; }
                    case "DC" -> { define(label, LC); int c=eval(t[i]); IC.add(String.format("%-4d (%s) (C,%d)", LC, OPTAB.get(op), c)); LC++; }
                    default -> {
                        if (label!=null) define(label, LC);
                        StringBuilder sb=new StringBuilder(String.format("%-4d (%s)", LC, OPTAB.get(op)));
                        for(;i<t.length;i++) sb.append(" ").append(encode(t[i]));
                        IC.add(sb.toString()); LC++;
                    }
                }
                if (op.equals("END")) break;
            }
        }

        print("*** INTERMEDIATE CODE ***\nLC \t IC", IC);
        printSymtab();
        printLittab();
        printPooltab();
    }

    static void define(String sym, int addr) {
        if (sym==null || sym.isEmpty()) return;
        sym=sym.replace(":","").toUpperCase();
        if (!SYMTAB.containsKey(sym)) SYMNAME.put(sym,"S"+(++symCount));
        SYMTAB.put(sym, addr);
    }

    static String encode(String op) {
        op=op.trim().toUpperCase();
        if (REGTAB.containsKey(op)) return "(RG,"+REGTAB.get(op)+")";
        if (CCTAB.containsKey(op)) return "(CC,"+CCTAB.get(op)+")";
        if (op.startsWith("=")) {
            if (!LITADDR.containsKey(op)) LITS.add(op);
            return "(L,"+(LITS.indexOf(op)+1)+")";
        }
        if (op.matches("\\d+")) return "(C,"+op+")";
        define(op, SYMTAB.getOrDefault(op, -1));
        return "(S,"+SYMNAME.get(op)+")";
    }

    static void closeLiterals() {
		if (nextLit < LITS.size()) {
			POOLTAB.add(nextLit + 1);  // mark the start of the new pool
			while (nextLit < LITS.size()) {
				String lit = LITS.get(nextLit);
				LITADDR.put(lit, LC);
				String val = lit.replaceAll("[^0-9]", "");
				IC.add(String.format("%-4d (DL,01) (C,%s)", LC, val));
				LC++;
				nextLit++;
			}
    }
}


    static int eval(String s) {
        s=s.toUpperCase();
        if (s.matches("\\d+")) return Integer.parseInt(s);
        if (s.contains("+")) { String[] x=s.split("\\+"); return SYMTAB.getOrDefault(x[0],0)+Integer.parseInt(x[1]); }
        if (s.contains("-")) { String[] x=s.split("-"); return SYMTAB.getOrDefault(x[0],0)-Integer.parseInt(x[1]); }
        return SYMTAB.getOrDefault(s,0);
    }

    static void print(String title, List<String> data) {
        System.out.println("\n"+title);
        data.forEach(System.out::println);
    }

    static void printSymtab() {
        System.out.println("\n*** SYMBOL TABLE ***\nIndex Symbol\tS-Name Address");
        int i=1;
        for (var e:SYMTAB.entrySet())
            System.out.printf("%-4d %-10s %-5s %d%n", i++, e.getKey(), SYMNAME.get(e.getKey()), e.getValue());
    }

    static void printLittab() {
        System.out.println("\n*** LITERAL TABLE ***\nIndex Literal\tAddress");
        int i=1;
        for (var e:LITADDR.entrySet())
            System.out.printf("%-4d %-10s %d%n", i++, e.getKey(), e.getValue());
    }

    static void printPooltab() {
        System.out.println("\n*** POOLTAB ***\nIndex\t Starting Index");
        int i=1;
        for (int n:POOLTAB)
            System.out.printf("%-4d %d%n", i++, n);
    }
}
