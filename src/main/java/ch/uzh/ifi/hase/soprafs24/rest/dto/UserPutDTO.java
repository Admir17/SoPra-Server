package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;

public class UserPutDTO {

    private LocalDate birthDate;

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
