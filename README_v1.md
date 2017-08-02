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
