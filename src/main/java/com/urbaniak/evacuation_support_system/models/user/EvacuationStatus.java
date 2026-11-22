package com.urbaniak.evacuation_support_system.models.user;

public enum EvacuationStatus {

     IDLE, /** nie uwzględniony w przypadku ewakuacji */
     ACTIVE, /** użytkownik uwzględniany w obliczeniach tras */
     ARRIVED /** dotarł do punktu ewakuacji, nie uwzględniony w ewakuacji */

}
