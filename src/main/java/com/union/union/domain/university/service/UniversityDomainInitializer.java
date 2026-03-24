package com.union.union.domain.university.service;

import com.union.union.domain.university.entity.UniversityDomain;
import com.union.union.domain.university.repository.UniversityDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversityDomainInitializer implements CommandLineRunner {

    private final UniversityDomainRepository universityDomainRepository;

    @Override
    public void run(String... args) throws Exception {
        if (universityDomainRepository.count() == 0) {
            log.info("대학교 도메인 초기 데이터를 삽입합니다.");
            List<UniversityDomain> domains = List.of(
                    new UniversityDomain("snu.ac.kr", "서울대학교"),
                    new UniversityDomain("yonsei.ac.kr", "연세대학교"),
                    new UniversityDomain("korea.ac.kr", "고려대학교"),
                    new UniversityDomain("sogang.ac.kr", "서강대학교"),
                    new UniversityDomain("skku.edu", "성균관대학교"),
                    new UniversityDomain("hanyang.ac.kr", "한양대학교"),
                    new UniversityDomain("cau.ac.kr", "중앙대학교"),
                    new UniversityDomain("khu.ac.kr", "경희대학교"),
                    new UniversityDomain("hufs.ac.kr", "한국외국어대학교"),
                    new UniversityDomain("uos.ac.kr", "서울시립대학교"),
                    new UniversityDomain("konkuk.ac.kr", "건국대학교"),
                    new UniversityDomain("dongguk.edu", "동국대학교"),
                    new UniversityDomain("hongik.ac.kr", "홍익대학교"),
                    new UniversityDomain("kookmin.ac.kr", "국민대학교"),
                    new UniversityDomain("ssu.ac.kr", "숭실대학교"),
                    new UniversityDomain("sejong.ac.kr", "세종대학교"),
                    new UniversityDomain("dankook.ac.kr", "단국대학교"),
                    new UniversityDomain("inu.ac.kr", "인천대학교"),
                    new UniversityDomain("gachon.ac.kr", "가천대학교"),
                    new UniversityDomain("kyonggi.ac.kr", "경기대학교")
            );
            universityDomainRepository.saveAll(domains);
            log.info("초기 대학교 도메인 {}개 삽입 완료", domains.size());
        } else {
            log.info("대학교 도메인 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
        }
    }
}
