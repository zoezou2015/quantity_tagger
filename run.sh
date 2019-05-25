# AddSub

#QT
java  -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.MathAddSubMain -lang en > qt_as
#QT(S)
java  -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.SemiMathAddSubMain  -lang en > sqt_as
#QT(R)
java  -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.RelaxMathAddSubMain  -lang en > rqt_as
#QT(FIX)
java -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.FixMathAddSubMain  -lang en > fqt_as


# AS_CN

#QT
java -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.VariantMathAddSubMain  -lang zh > qt_ma
#QT(S)
java -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.SemiMathAddSubMain -lang zh > sqt_ma
#QT(R)
java -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.RelaxMathAddSubMain -lang zh > rqt_ma
#QT(FIX)
java -Djava.library.path=/usr/local/lib -cp bin/math_addsub.jar org.nlp.example.FixMathAddSubMain -lang zh > fqt_ma

