package service;


import dto.reponse.CharityResponse;
import dto.request.CharityRequest;
import dto.request.CharitySelectionRequest;
import entity.Charity;
import entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CharityContributionRepository;
import repository.CharityRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharityService {

    private final CharityRepository charityRepository;
    private final CharityContributionRepository contributionRepository;
    private final UserRepository userRepository;

    // Public: list all active charities
    public List<CharityResponse> getAllActive() {
        return charityRepository.findByIsActiveTrue()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Public: featured charities for homepage
    public List<CharityResponse> getFeatured() {
        return charityRepository.findByIsFeaturedTrueAndIsActiveTrue()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Public: search by name
    public List<CharityResponse> search(String query) {
        return charityRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(query)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Public: get single charity
    public CharityResponse getById(UUID id) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found"));
        return mapToResponse(charity);
    }

    // User: select charity and set contribution percentage
    @Transactional
    public void selectCharity(String email, CharitySelectionRequest request) {


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        Charity charity = charityRepository.findById(request.getCharityId())
                .orElseThrow(() -> new RuntimeException("Charity not found"));

        if (!charity.getIsActive()) {
            throw new RuntimeException("Charity is not active");
        }

        user.setCharityId(request.getCharityId());
        user.setCharityPercentage(request.getCharityPercentage());
        userRepository.save(user);
    }

    // Admin: create charity
    @Transactional
    public CharityResponse create(CharityRequest request) {
        Charity charity = Charity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .websiteUrl(request.getWebsiteUrl())
                .isFeatured(request.getIsFeatured())
                .isActive(true)
                .build();
        return mapToResponse(charityRepository.save(charity));
    }

    // Admin: update charity
    @Transactional
    public CharityResponse update(UUID id, CharityRequest request) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found"));

        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setImageUrl(request.getImageUrl());
        charity.setWebsiteUrl(request.getWebsiteUrl());
        charity.setIsFeatured(request.getIsFeatured());

        return mapToResponse(charityRepository.save(charity));
    }

    // Admin: soft delete (deactivate)
    @Transactional
    public void deactivate(UUID id) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found"));
        charity.setIsActive(false);
        charityRepository.save(charity);
    }

    private CharityResponse mapToResponse(Charity charity) {
        BigDecimal total = contributionRepository.sumAmountByCharityId(charity.getId());
        return CharityResponse.builder()
                .id(charity.getId())
                .name(charity.getName())
                .description(charity.getDescription())
                .imageUrl(charity.getImageUrl())
                .websiteUrl(charity.getWebsiteUrl())
                .isFeatured(charity.getIsFeatured())
                .isActive(charity.getIsActive())
                .totalContributions(total != null ? total : BigDecimal.ZERO)
                .build();
    }
}