package nbt_test_0001;

import java.io.*;
import net.querz.nbt.*;

/**
 * 
 * @author Trecar
 * 
 * @version 0.2
 * 
 * This document: Copyright (c) 2020 Trecar
 * 
 * NBT Library:   Copyright (c) 2016 Querz
 * 
 * Library can be accessed at github.com/Querz/NBT/
 * 
 * BOTH WORKS ARE LICENSED UNDER THE MIT LICENSE AS SEEN BELOW:
 * 
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

public class CPU01assembler {

    public static byte[] blockData = new byte[12672];
    public static byte[] colorCode = {8, 8, 8, 8, 7, 7, 7, 6, 6, 6, 5, 4, 3, 3, 3, 0};
	
	public static void main(String[] args){
		
        assembleAllText();
		
	}

	public static boolean[] binToBool(String binStr){
		char[] valarr=binStr.toCharArray();
		boolean[] temp_output=new boolean[valarr.length];
		for(int i=0; i<valarr.length; i++){
			if(valarr[i]=='1'){
				temp_output[i]=true;
			}
		}
		return temp_output;
	}
	
	public static void initFill(){
		for(int i=0; i<blockData.length-1; i++){
			blockData[i]=2;
		}
		blockData[blockData.length-1]=9;
	}

	public static void writeLine(int index, String instruction){

		//Convert Instruction from binary String to boolean Array
		boolean[] instArray=binToBool(instruction);

		//Determining location of Instruction Starting Block (LSB of OpCode)
		int level=(index<16)?0 : (index<32)?1 : (index<48)?2 : 3;
		int x0=30-2*(index-16*level);
		int y0=9-3*level;
		int z0=32-level%2;

		//Placing the Blocks
		int zTemp;
		for(int i=0; i<16; i++){
			zTemp=z0-2*i;
			
			//for the 2-wide Gap in the ROM
			if(i>7){
				zTemp=zTemp-1;
			}
			
			//Place Terracotta Block as per colorcode
			blockData[x0+zTemp*32+y0*32*33]=colorCode[i];
			
			//Place Torches or Glass Block
			if(instArray[i]){
				blockData[x0+1+zTemp*32+y0*32*33]=1;
			}
			else{
				blockData[x0+1+zTemp*32+y0*32*33]=10;
			}
		}
	}

    //Places a Block for easy WE pasting
	public static void wePosBlock(int codeLength){
		int level=(codeLength<=16)?0 : (codeLength<=32)?1 : (codeLength<=48)?2 : 3;
		//% stuff for place white terra in level 2, otherwise regular brown terra
		if(level>0){
			blockData[(9-3*level)*32*33]=(byte)(0+9*(1-level%2));
		}
	}
	
	//Take String-Array of 16-Bit binary instruction Strings and write to BlockData, starting at line 0
	//Also fills with Air and places W/E Block
	public static void writeData(String[] data) {
		initFill();
		for(int i=0; i<data.length; i++) {
			writeLine(i, data[i]);
		}
		wePosBlock(data.length);
	}
	
    //Writes BlockData in byteArray into a Schematic file using Querz' NBT library github.com/Querz/NBT/ (License: MIT)
    public static void writeSchem(byte[] data_input, String schemTitle) {
		
    	//Transform BlockData into ByteArrayTag
		ByteArrayTag dataTag=new ByteArrayTag(data_input);
		
		//Create and write Schematic Data to wrapping CompoundTag
		CompoundTag wrapper=new CompoundTag();
		
		wrapper.putInt("PaletteMax", 11);
		
		CompoundTag palette=new CompoundTag();
		palette.putInt("minecraft:brown_terracotta", 0);
		palette.putInt("minecraft:redstone_wall_torch[facing=east,lit=true]", 1);
		palette.putInt("minecraft:air", 2);
		palette.putInt("minecraft:yellow_terracotta", 3);
		palette.putInt("minecraft:terracotta", 4);
		palette.putInt("minecraft:light_gray_terracotta", 5);
		palette.putInt("minecraft:green_terracotta", 6);
		palette.putInt("minecraft:lime_terracotta", 7);
		palette.putInt("minecraft:light_blue_terracotta", 8);
		palette.putInt("minecraft:white_terracotta", 9);
		palette.putInt("minecraft:glass", 10);
		wrapper.put("Palette", palette);
		
		wrapper.putInt("Version", 2);
		wrapper.putShort("Length", (short)33);
		
		CompoundTag meta=new CompoundTag();
		meta.putInt("WEOffsetX", -31);
		meta.putInt("WEOffsetY", -12);
		meta.putInt("WEOffsetZ", -32);
		wrapper.put("Metadata", meta);
		
		wrapper.putShort("Height", (short)12);
		wrapper.putInt("DataVersion", 1976);
		wrapper.put("BlockData", dataTag);
		
		//Dummy List. Not sure if StringTag correct for this. Probably not.
		ListTag<StringTag> listDummy = new ListTag<>(StringTag.class);
		wrapper.put("BlockEntities", listDummy);
		
		wrapper.putShort("Width", (short)32);
		
		//The Values here do not matter, but it is required.
		int[] offsetArray= {420, 69, 1337};
		IntArrayTag offset=new IntArrayTag(offsetArray);
		wrapper.put("Offset", offset);
		
		//Write file. Note the "Schematic"-Tag for the wrapping CompoundTag as required for Schematics.
		try{
        	NBTUtil.writeTag(wrapper, "Schematic" , schemTitle + ".schem");
        	System.out.println("File "+schemTitle+".schem created successfully!");
		}catch(IOException e){e.printStackTrace();}
		
	}
    
    //Many thanks to StackOverflow User "David Cheung"
    //Gets Filename as a String, returns Base. Should not be NULL
    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }
    
    public static void assembleAllText() {
    	
    	//Get all .txt Files in current directory (case insensitive regarding suffix)
    	File directory=new File(".");
		File[] textFiles = directory.listFiles((File pathname) -> pathname.getName().toLowerCase().endsWith(".txt"));
		
		//Compile a .schem of same name for every .txt (THUS ALL TXT IN DIRECTORY MUST BE COMPILEABLE)
		for(int i=0; i<textFiles.length; i++) {
			
			//Get file and its name
			File tempFile=textFiles[i];
			String tempName=getBaseName(tempFile.getName());
			
			//Read data from .txt, no more than 64 lines. 
			String[] rawData=new String[64];
			int ind=0;
			try{
				BufferedReader dataslave=new BufferedReader(new FileReader(tempFile));
				String tempLine;
				while((tempLine=dataslave.readLine())!=null && ind<rawData.length) {
					rawData[ind]=tempLine;
					ind++;
				}
				dataslave.close();
			}catch(IOException e){e.printStackTrace();}
			
			//Create Array of actual input length (Nothing null)
			String[] dataOut=new String[ind];
			
			//Allow .jar to skip invalid .txt files
			try {
				//Compile everything =/= null into Array dataOut
				for(int i2=0; i2<dataOut.length; i2++) {
					dataOut[i2]=assembleLine(rawData[i2]);
				}
				
				//Write compiled data into the blockData Array
				writeData(dataOut);
				//Create Schematic from blockData Array
				writeSchem(blockData, tempName);
			}catch(Exception e) {e.printStackTrace();}
		}
    }
    
    //Actually does the interesting bit.
    public static String assembleLine(String expr) {
    	String output="";
    	String[] splitArray=expr.toLowerCase().split(" ");
    	
    	//This little part with two loops allows you to use as many spaces as you want between expressions
    	//Because I do not want the assembler to break once you accidentally use two spaces
    	int temp_val=0;
    	for(int i=0; i<splitArray.length; i++) {
    		if(splitArray[i].equals("")) {
    			temp_val++;
    		}
    	}
    	String[] exprArray=new String[splitArray.length-temp_val];
    	temp_val=0;
    	for(int i=0; i<splitArray.length; i++) {
    		if(!splitArray[i].equals("")) {
    			exprArray[temp_val]=splitArray[i];
    			temp_val++;
    		}
    	}
    	
    	//Mapping every possible OpCode to a numerical value
    	String[] opMap= {"nop", "add",   "sub",  "or",  "nand", "xor", "adc", "and",   "addi", "subi", 
    			         "ori", "nandi", "xori", "lim", "andi", "shr", "swp", "swpnt", "jmp",  "beq", 
    			         "bgt", "bge",   "bue",  "bzr", "bnt",  "btr", "buf", "inp",   "out",  "setup"};
    	
    	//Determining OpCode. Exception is necessary here, otherwise cannot terminate
    	int opIndex=0;
    	while(!exprArray[0].equals(opMap[opIndex])) {
    		opIndex++;
    		if(opIndex==opMap.length) {
    			throw new RuntimeException("OpCode \""+exprArray[0].toUpperCase()+ "\" is not a valid OpCode");
    		}
    	}
    	
    	//Handle the add1 option. String will be added to binary data
    	String add1Tag="0";
    	if(opIndex==1 && exprArray.length>4 && exprArray[4].equals("add1")) {
    		add1Tag="1";
    	}
    	
    	//Handle addCarry. String will be added to binary data
    	String adcTag="0";
    	if(opIndex==6) {
    		adcTag="1";
    		opIndex=1;
    	}
    	//Handle diversion of btr and buf into two OpCodes for better readability
    	if(opIndex==26) {
    		opIndex=25;
    	}
    	
    	//Determine layer for switch
    	int layer=(opIndex<1)?0 :  (opIndex<8)?1 : (opIndex<15)?2 : (opIndex<16)?3 : (opIndex<17)?4 :
    		     (opIndex<18)?5 : (opIndex<27)?6 : (opIndex<28)?7 : (opIndex<29)?8 : 9;
    	
    	//Check the branching options if needed
    	String stall="0", noSave="0";
    	if(layer==6) {
    		for(int i=0; i<exprArray.length; i++) {
    			if(exprArray[i].equals("st")) {
    				stall="1";
    			}
    			if(exprArray[i].equals("ns")) {
    				noSave="1";
    			}
    		}
    	}
    	
    	//Get Options if needed
    	String zeroReg="0", display2C="0";
    	if(layer==9) {
    		for(int i=0; i<exprArray.length; i++) {
    			if(exprArray[i].equals("zrr")) {
    				zeroReg="1";
    			}
    			if(exprArray[i].equals("2c")) {
    				display2C="1";
    			}
    		}
    	}
    	
    	//Does remaining Assembly in this switch
    	switch(layer) {
    	
    	//NOP
    	case 0 :
    		output="0000000000000000";
    		break;
    	
    	//ALU 3OP with ADC	
    	case 1 :
    		output=asbNum(opIndex+"d", 4) + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3) + 
    			   asbNum(exprArray[2].charAt(exprArray[2].length()-1)+"d", 3) + adcTag + add1Tag + 
    		       asbNum(exprArray[3].charAt(exprArray[3].length()-1)+"d", 3)+"0";
    		break;
    		
    	//ALU IMM
    	case 2 :
    		output=asbNum((opIndex-7)+"d", 4) + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3) + asbNum(exprArray[2], 8)+ "1";
    		break;
    		
    	//ALU SHR
    	case 3 :
    		output="0110" + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3) + "00000" + 
    	           asbNum(exprArray[2].charAt(exprArray[2].length()-1)+"d", 3) + "0";
    		break;
    		
    	//RAM SWP optional OR with pointer
    	case 4 :
    		output="0001" + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3) + asbNum(exprArray[2], 5) + 
    				asbNum(exprArray[3].charAt(exprArray[3].length()-1)+"d", 3);
    		
    		if(exprArray.length>4 && exprArray[4].equals("pnt")) {
    			output+="1";
    		}else {
    			output+="0";
    		}
    		break;
    		
    	//RAM SWPNT always uses pointer no number input acceptable
    	case 5 :
    		output="0001" + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3) + "00000" + 
			       asbNum(exprArray[2].charAt(exprArray[2].length()-1)+"d", 3) + "1";
    		break;
    		
    	//Branching (conditional & unconditional)
    	case 6 :
    		output="1001" + asbNum(exprArray[1], 6) + noSave + stall + asbNum((opIndex-18)+"d", 3) + "0";
    		break;
    		
    	//INPUT
    	case 7 :
    		String inPort=asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 2);
    		output="0101" + asbNum(exprArray[1], 6) + inPort.charAt(0);
    		if(exprArray.length>4 && exprArray[4].equals("st")) {
    			output+="1";
    		}else {
    			output+="0";
    		}
    		output+=asbNum(exprArray[2].charAt(exprArray[2].length()-1)+"d", 3) + inPort.charAt(1);
    		break;
    	
    	//OUTPUT
    	case 8 :
    		String outPort=asbNum(exprArray[2].charAt(exprArray[2].length()-1)+"d", 2);
    		output="1101" + asbNum(exprArray[1].charAt(exprArray[1].length()-1)+"d", 3);
    		if(exprArray.length>3 && exprArray[3].equals("oir")) {
    			output+="100";
    		}else {
    			output+="000";
    		}
    		output+=outPort.charAt(0) + "0000" +outPort.charAt(1);
    		break;
    		
    	//SetUp instruction
    	case 9 :
    		output="00001" + zeroReg + display2C + "000000000";
    		break;
    	}
    	
    	//Return completed binary instruction
    	return output;
    }
    
    //Number representation: Suffix d, h, b (decimal, hex, binary)
    //Example: "a9h", "253d", "10d", "-113d", "10110b"
    //Negative decimals will be converted to 8bit 2's Complement
    //Syntax: Number as String, length of output binary String.
    //Cuts number off or adds 0 until length satisfied
    //Output: binary String with MSB ON THE RIGHT because that's how the CPU is wired.
	public static String asbNum(String numStr, int lgth) {
		
		char numType = numStr.charAt(numStr.length()-1);
		String numPart=numStr.substring(0, numStr.length()-1);
		
		//Convert everything to Integer and then back to binary like an absolute madman
		int numval=0;
		switch(numType) {
		case 'd':
			numval=Integer.parseInt(numPart);
			
			//Amazing 2's C Implementation right there
			if(numval<0) {
				numval=256+numval;
			}
			break;
		case 'h':
			numval=Integer.parseInt(numPart, 16);
			break;
		case 'b':
			numval=Integer.parseInt(numPart, 2);
			break;
		}
		
		String valstr=Integer.toBinaryString(numval);
		char[] valarr=valstr.toCharArray();
		
		String temp_out="";
		for(int i=0; i<lgth; i++) {
			if(i<valarr.length) {
				temp_out=temp_out+valarr[valarr.length-1-i];
			}
			else {
				temp_out=temp_out+"0";
			}
		}
		return temp_out;
	}
	
}
