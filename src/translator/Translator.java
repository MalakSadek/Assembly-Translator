
package translator;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Translator {

public static void main(String[] arg)
{
    String[] inputs = new String[20];
    String[] code = new String[20];
    int[] PC = new int[1];
    int i = 0;
    PC[0] = 0;
    Map labels = new HashMap();
    File InstructionFile = new File("Instructions.txt");
    
    try {
        Scanner input = new Scanner(InstructionFile);
      while (input.hasNextLine()) // dah hyb2a while !.eof()
    {
        inputs[i] = input.nextLine();
        System.out.printf(inputs[i]);
        System.out.println();
        Pattern labelformat = Pattern.compile("((.*)(\\:))");
        Matcher labelformatmatcher = labelformat.matcher(inputs[i]);
        boolean b = labelformatmatcher.matches();
        if (b)
              labels.put(labelformatmatcher.group(2), i);
        i++;
    }
    
    } catch (FileNotFoundException e) 
    {
        System.out.println("There was an error: "+ e.toString());
    }
 
    for (int j = 0; j < 6; j++) // w dah kaman
    {
        code[PC[0]/4] = machinecode(inputs[PC[0]/4], labels, PC);
        System.out.printf(labels.toString());
        System.out.printf("PC: " +PC[0]);
        System.out.println();
    }
   
}


public static String machinecode(String Instruction, Map labels, int[] PC)
{
        boolean branch_taken = true;
        char type = 'X';
        String Instr = "0", assembly = "0", funct = "0";
        int reg_d = -1, reg_s = -1, reg_t = -1, imm = -1, offset = -1, branch = -1, jump = -1;
        
        System.out.println(Instruction);
        System.out.println();
        Pattern labelformat = Pattern.compile("((.*)(\\:))");
        Matcher labelformatmatcher = labelformat.matcher(Instruction);
        boolean bL = labelformatmatcher.matches();
        Pattern Rformat = Pattern.compile("((.*) (\\d)\\, (\\d)\\, (\\d))");
        Matcher Rformatmatcher = Rformat.matcher(Instruction);
        boolean bR = Rformatmatcher.matches();
        Pattern Iformat = Pattern.compile("((.*) (\\d)\\, (\\d)\\, (\\+|\\-)(\\d))");
        Matcher Iformatmatcher = Iformat.matcher(Instruction);
        boolean bI = Iformatmatcher.matches();
        Pattern Bformat = Pattern.compile("((.*) (\\d)\\, (\\d)\\, (.*))");
        Matcher Bformatmatcher = Bformat.matcher(Instruction);
        boolean bB = Bformatmatcher.matches();
        Pattern Wformat = Pattern.compile("((.*) (\\d)\\, (\\d)\\((\\d)\\))");
        Matcher Wformatmatcher = Wformat.matcher(Instruction);
        boolean bW = Wformatmatcher.matches();
        Pattern Jformat = Pattern.compile("((.*) (.*))");
        Matcher Jformatmatcher = Jformat.matcher(Instruction);
        boolean bJ = Jformatmatcher.matches();
        if (bL)
        {
            type = 'L';
            PC[0] = PC[0] + 4;
        }
        else if (bR) // rtype
        {
            type = 'R';
            Instr = Rformatmatcher.group(2);
            reg_d = Integer.parseInt(Rformatmatcher.group(3));
            reg_s = Integer.parseInt(Rformatmatcher.group(4));
            reg_t = Integer.parseInt(Rformatmatcher.group(5));
        }
        else if (bI)  //itype
        {
            type = 'I';
            Instr = Iformatmatcher.group(2);
            reg_d = Integer.parseInt(Iformatmatcher.group(3));
            reg_s = Integer.parseInt(Iformatmatcher.group(4));
            if(Iformatmatcher.group(5).equals("-"))
            imm = -Integer.parseInt(Iformatmatcher.group(6));
            else
            imm = Integer.parseInt(Iformatmatcher.group(6)); 
        }
        else if (bB)  //branch
        {
            type = 'I';
            Instr = Bformatmatcher.group(2);
            reg_d = Integer.parseInt(Bformatmatcher.group(3));
            reg_s = Integer.parseInt(Bformatmatcher.group(4));
            branch = (int) labels.get(Bformatmatcher.group(5));
        }
        else if (bW) //lw sw
        {
            type = 'I';
            Instr = Wformatmatcher.group(2);
            reg_d = Integer.parseInt(Wformatmatcher.group(3));
            reg_s = Integer.parseInt(Wformatmatcher.group(5));
            offset = Integer.parseInt(Wformatmatcher.group(4));
        }
        else if (bJ) // j type
        {
            type = 'J';
            Instr = Jformatmatcher.group(2);
            jump = (int) labels.get(Jformatmatcher.group(3));
        }
        else
        {
            System.out.printf("No match.");
            System.out.println();
        }
        System.out.printf("Instruction Type: " +type +"\nInstruction: " +Instr + "\nDestination Register: " +reg_d +"\nSource Register: " +reg_s +"\nT Register: " +reg_t +"\nImmediate Value: " +imm +"\nBranch Destination: " +branch +"\nLW/SW Offset: " +offset +"\nJump Destination: " +jump +"\n");
           
    switch (type) {
        case 'R':
            switch (Rformatmatcher.group(2)) {
                case "ADD":
                    funct = "100000";
                    break;
                case "XOR":
                    funct = "100110";
                    break;
                case "SLT":
                    funct = "101010";
                    break;
                case "JR":
                    funct = "001000";
                    break;
                default:
                    break;
            }
            assembly = "000000" + toBinaryaddr(reg_s) + toBinaryaddr(reg_t) + toBinaryaddr(reg_d) + "00000" + funct;
            PC[0] = PC[0] + 4;
            break;
        case 'I':
            if(bI)
            {if(Iformatmatcher.group(2).equals("ADDI"))
            {
                funct = "001001";
                assembly = funct + toBinaryaddr(reg_s) + toBinaryaddr(reg_d) + toBinaryimm(imm);
                PC[0] = PC[0] + 4;
              
            }}
            else if(bW) {if (Wformatmatcher.group(2).equals("SW"))
            {
                funct = "101011";
                assembly = funct + toBinaryaddr(reg_d) + toBinaryaddr(reg_s) + toBinaryimm(offset);
                PC[0] = PC[0] + 4;
               
            }
            else if (Wformatmatcher.group(2).equals("LW"))
            {
                funct = "100011";
                assembly = funct + toBinaryaddr(reg_s) + toBinaryaddr(reg_d) + toBinaryimm(offset);
                PC[0] = PC[0] + 4;
             
            }}
            else if(bB) {if (Bformatmatcher.group(2).equals("BLE"))
            {
                funct = "000110";
                PC[0] = PC[0] + 4;
                assembly = funct + toBinaryaddr(reg_s) + toBinaryaddr(reg_d) + toBinaryPC(branch);
                if(branch_taken)
                    PC[0] = (branch * 4);
                
            }}
            break;
        case 'J':
            switch (Jformatmatcher.group(2)) {
                case "JAL":
                    funct = "000011";
                    break;
                case "J":
                    funct = "000010";
                    break;
                case "Jump_Procedure":
                    funct = "000000";
                    break;
                case "Return_Procedure":
                    funct = "000001";
                    break;
                default:
                    break;
            }
            PC[0] = PC[0] + 4;
            assembly = funct + toBinaryPC(jump);
            PC[0] = (jump * 4);
            break;
        default:
            break;
    }

    System.out.printf("Machine Code: " +assembly +"\n"); 
    return assembly;
}

public static String toBinaryaddr(int n)
{
    String r = new String();
    while(n!=0) {r=(n%2==0 ?"0":"1")+r; n/=2;}
    while(r.length()<5){r = "0" + r;}
        
    return r;
}
public static String toBinaryimm(int n)
{
    String r = new String();
    while(n!=0) {r=(n%2==0 ?"0":"1")+r; n/=2;}
    
    while(r.length()<16)
    {
        if(r.charAt(r.length()-1) == '0')
        r = "0" + r;
        if(r.charAt(r.length()-1) == '1')
        r = "1" + r;
    }
    
    return r;
}
public static String toBinaryPC(int n)
{
    String r = new String();
    while(n!=0) {r=(n%2==0 ?"0":"1")+r; n/=2;}
    r = r + "00";
    while(r.length()<16)
    {
    if(r.charAt(r.length()-1) == '0')
        r = "0"+ r;
    if(r.charAt(r.length()-1) == '1')
        r = "1"+ r;
    }
    
    return r;
}

    
}
