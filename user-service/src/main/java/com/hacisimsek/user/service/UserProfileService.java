package com.hacisimsek.user.service;

import com.hacisimsek.user.dto.AddressRequest;
import com.hacisimsek.user.dto.CreateProfileRequest;
import com.hacisimsek.user.dto.PreferencesRequest;
import com.hacisimsek.user.dto.UpdateProfileRequest;
import com.hacisimsek.user.model.Address;
import com.hacisimsek.user.model.UserPreferences;
import com.hacisimsek.user.model.UserProfile;
import com.hacisimsek.user.repository.AddressRepository;
import com.hacisimsek.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    // ── Profile ───────────────────────────────────────────────────────────────

    /**
     * Creates a profile for a newly registered user.
     * Called with the same UUID that auth-service assigned to the user.
     */
    @Transactional
    public UserProfile createProfile(UUID userId, CreateProfileRequest request) {
        if (profileRepository.existsById(userId)) {
            throw new RuntimeException("Profile already exists for user: " + userId);
        }
        UserProfile profile = UserProfile.builder()
                .id(userId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .preferences(new UserPreferences())
                .build();
        UserProfile saved = profileRepository.save(profile);
        log.info("Profile created for user: {}", userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(UUID userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }

    @Transactional
    public UserProfile updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = getProfile(userId);
        profile.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        return profileRepository.save(profile);
    }

    @Transactional
    public void deactivateProfile(UUID userId) {
        UserProfile profile = getProfile(userId);
        profile.setAccountStatus(com.hacisimsek.user.model.AccountStatus.DEACTIVATED);
        profileRepository.save(profile);
        log.info("Profile deactivated for user: {}", userId);
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    @Transactional
    public UserProfile updatePreferences(UUID userId, PreferencesRequest request) {
        UserProfile profile = getProfile(userId);
        UserPreferences prefs = profile.getPreferences();
        if (prefs == null) prefs = new UserPreferences();

        if (request.getLanguage() != null)          prefs.setLanguage(request.getLanguage());
        if (request.getCurrency() != null)          prefs.setCurrency(request.getCurrency());
        if (request.getEmailNotifications() != null) prefs.setEmailNotifications(request.getEmailNotifications());
        if (request.getSmsNotifications() != null)  prefs.setSmsNotifications(request.getSmsNotifications());
        if (request.getPushNotifications() != null) prefs.setPushNotifications(request.getPushNotifications());

        profile.setPreferences(prefs);
        return profileRepository.save(profile);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Address> getAddresses(UUID userId) {
        // ensure profile exists
        getProfile(userId);
        return addressRepository.findByUserProfileId(userId);
    }

    @Transactional
    public Address addAddress(UUID userId, AddressRequest request) {
        UserProfile profile = getProfile(userId);

        // If this address is default, clear existing default first
        if (request.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .userProfile(profile)
                .label(request.getLabel())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .defaultAddress(request.isDefaultAddress())
                .build();

        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserProfileId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (request.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(userId);
        }

        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(request.isDefaultAddress());

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserProfileId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    @Transactional
    public Address setDefaultAddress(UUID userId, UUID addressId) {
        addressRepository.clearDefaultForUser(userId);
        Address address = addressRepository.findByIdAndUserProfileId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        address.setDefaultAddress(true);
        return addressRepository.save(address);
    }
}
