### Pref Data – the Android SharedPreferences wrapper

#### HelloWorld
1) Create the interface and annotate it with *@PrefData*
```java
@PrefData
public interface UserParams {
    int getAge();
    void setAge(int age);
}
```

2) Create instance of generated class (it will be prefixed by "Pref")
```java
SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
UserParams userParams = new PrefUserParams(prefs);
```

#### Download
```groovy
apt 'org.xelevra.libs:prefdata-processor:0.3'
provided 'org.xelevra.libs:prefdata-annotations:1.1'
```

#### Usage

1) Chains
```java
UserParams setAge(int age);
UserParams setName(String name);
```
2) Default value
```java
int getAge(int defAge);
```
3) Prefix
```java
int getChildAge(@Prefix String childName);
int getChildAge(@Prefix String childName, int defaultAge);
void setChildAge(@Prefix String childName, int age);
```
4) Buffered edition
```java
UserParams setName(String name);
UserParams setAge(int age);    

UserParams edit();
void apply();
// and/or
void commit();
```
Example:
```java
userParams.edit().setAge(32).setName("Bob").apply();
```

#### Supported types
Already supported only primitive types, its boxings and String

#### Limit the range of allowed values

For the limiting possible values for field, use `@Belongs` annotation.

Example:
```java
@Belongs("animals", "plants", "fungi", "chromista", "protist")
String eukaryoteKingdom

@Belongs("2", "3", "5", "7")
int smallPrimeNumber

@Belongs("0", "999999999999")
long npePerProject

@Belongs("-1", "0.43", "54.444f")
float randomNumber
```

**Note that** following examples will not compile:
```java
@Belongs("100", "50.5", "I don't know", "false", "90000000000")
int playerHp
//since floats, strings, booleans and longs are not allowed for ints

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
long playerHp
//since floats, strings, booleans are not allowed for longs

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
boolean whyIParticipatingInThis
//since only "true" and "false" allowed for booleans

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
float playerHp
//since strings, booleans are not allowed for floats
```
