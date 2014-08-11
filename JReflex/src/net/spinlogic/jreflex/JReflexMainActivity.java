package net.spinlogic.jreflex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class JReflexMainActivity extends Activity {
	
	public static final String FN_JRDIR		= "jReflex";
	
	private File logFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jreflex_main);
	}
	
	public void processGetClassBtnClick(View view) {
		EditText ed_classname 	= (EditText) findViewById(R.id.et_class);
		String classname 		= ed_classname.getText().toString();
		String result_txt		= getString(R.string.txt_result_empty);
		
		if(!classname.isEmpty()) {
			result_txt = getClassInfo(classname);
		}
		Toast.makeText(this, result_txt, Toast.LENGTH_SHORT).show();
	}
	
	
	private String getClassInfo(String classname) {
		int index = classname.lastIndexOf(".");
		String lfn = (index != -1) ? classname.substring(index + 1) : classname;
		String info = "";
		String result = "";
		try {
			// Class name
			Class<?> mClass = Class.forName(classname);
			createLogFile(lfn);	// At this point we know that the class exists
			appendLine(getString(R.string.txt_class) + " \n   " + mClass.getCanonicalName() + "\n");
			
			// Package
			Package p = mClass.getPackage();
			info = (p != null) ? p.getName() : getString(R.string.txt_nopackage);
			appendLine(getString(R.string.txt_package) + "\n   " + info + "\n");
			info = "";
			
			// Modifiers
			info = Modifier.toString(mClass.getModifiers());
			appendLine(getString(R.string.txt_modifiers) + "\n   " + info + "\n");
			info = "";
			
			// Parameters
			TypeVariable[] tv = mClass.getTypeParameters();
			if(tv.length == 0) {
				info = "   " + getString(R.string.txt_notypeparams) + "\n";
			}
			else {
				for (TypeVariable t : tv) {
					info += t.getName() + ", ";
				}
			}
			appendLine(getString(R.string.txt_typeparams) + "\n" + info);
			info = "";
			
			// Interfaces
			Type[] intfs = mClass.getGenericInterfaces();
			if (intfs.length == 0) {
				info = "   " + getString(R.string.txt_noifs) + "\n";
			} else {
				for (Type intf : intfs) {
				    info += "   " + intf.toString() + "\n";
				}
			}
			appendLine(getString(R.string.txt_ifs) + "\n" + info);
			info = "";
			
			// Inheritance
			List<Class> l = new ArrayList<Class>();
		    getAncestors(mClass, l);
		    if (l.size() == 0) {
		    	info = "   " + getString(R.string.txt_nosuper) + "\n";
		    }
		    else {
		    	for (Class<?> cl : l) {
		    		info += "   " + cl.getCanonicalName() + "\n";
		    	}
		    }
		    appendLine(getString(R.string.txt_inheritance) + "\n" + info);
			info = "";
			
			// Annotations
			Annotation[] ann = mClass.getAnnotations();
		    if (ann.length == 0) {
		    	info = "   " + getString(R.string.txt_noannot) + "\n";
		    }
		    else {
				for (Annotation a : ann) {
					info +=  "   " + a.toString() + "\n";
				}
		    }
		    appendLine(getString(R.string.txt_annotations) + "\n" + info);
			info = "";
			
			// Constructors
			appendLine(getString(R.string.txt_constructors));
			info = getMembers(mClass.getConstructors());
			if(info.isEmpty()) {
				info = "   " + getString(R.string.txt_noconstructors);
			}
			appendLine(info + "\n");
			info = "";
			
			// Fields
			appendLine(getString(R.string.txt_fields));
			info = getMembers(mClass.getDeclaredFields());
			if(info.isEmpty()) {
				info = "   " + getString(R.string.txt_nofields) + "\n";
			}
			appendLine(info);
			info = "";
			
			// Methods
			appendLine(getString(R.string.txt_methods));
			info = getMembers(mClass.getMethods());
			if(info.isEmpty()) {
				info = "   " + getString(R.string.txt_nomethods) + "\n";
			}
			appendLine(info);
			info = "";
			
			// Inner classes
			appendLine(getString(R.string.txt_classes));
			info = getClasses(mClass);
			if(info.isEmpty()) {
				info = "   " + getString(R.string.txt_noclasses) + "\n";
			}
			appendLine(info);
			info = "";
			
			result = getString(R.string.txt_tv_result_ok) + " " + logFile.getPath();
		} catch (ClassNotFoundException e) {
			result = getString(R.string.txt_result_noclass);
		} catch (IOException e) {
			result = getString(R.string.txt_result_logfileerr);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			result = getString(R.string.txt_result_fieldaccess);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			result = getString(R.string.txt_result_fieldaccess);
		}
		return result;
	}
	
	
	private void getAncestors(Class<?> c, List<Class> l) {
		Class<?> ancestor = c.getSuperclass();
	 	if (ancestor != null) {
		    l.add(ancestor);
		    getAncestors(ancestor, l);
	 	}
	}
	
	
	private String getMembers(Member[] mbrs) throws IllegalAccessException, IllegalArgumentException {
		String result = "";
		for (Member mbr : mbrs) {
		    if (mbr instanceof Field) {
		    	((Field)mbr).setAccessible(true);
		    	int modif = ((Field)mbr).getModifiers();
		    	int fval = -1;
		    	if((modif & 0x0042) > 0) { // FINAL and STATIC
		    		if(((Field)mbr).getType() == Integer.TYPE) {
		    			fval = ((Field)mbr).getInt(null);
		    		}
		    	}
		    	result += "   " + ((Field)mbr).toGenericString();
		    	if(fval >= 0) result += " = " + Integer.toString(fval);
		    	result += "\n";
		    }
		    else if (mbr instanceof Constructor) {
		    	result += "   " + ((Constructor)mbr).toGenericString() + "\n";
		    }
		    else if (mbr instanceof Method) {
		    	((Method)mbr).setAccessible(true);
		    	result += "   " + ((Method)mbr).toGenericString() + "\n";
		    }
		}
		return result;
	}
	

	private String getClasses(Class<?> c) {
		String result = "";
		Class<?>[] clss = c.getClasses();
		for (Class<?> cls : clss) {
			result += "   " + cls.getCanonicalName() + "\n";
		}
		return result;
	}
	
	
	private void createLogFile(String filename) throws IOException {
		filename += ".txt";	// set text extension to the file
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File path = new File(Environment.getExternalStorageDirectory() + File.separator + FN_JRDIR);
			//	Make directory if it does not exist
			if(!path.exists()) {
				path.mkdir();
			}
			logFile = new File(path, filename);
			if(logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();
		}
	}
	
	
	private void appendLine(String line) throws IOException {
		BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		buf.append(line);
		buf.newLine();
		buf.close();
	}
}
