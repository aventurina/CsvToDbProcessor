package com.iheartsimplelife;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<TerritoryAddress> reader() {
        FlatFileItemReader<TerritoryAddress> reader = new FlatFileItemReader<TerritoryAddress>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<TerritoryAddress>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "fullName", "street", "city", "state", "zipCode", "notes" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<TerritoryAddress>() {{
                setTargetType(TerritoryAddress.class);
            }});
        }});
        return reader;
    }

    @Bean
    public TerritoryItemProcessor processor() {
        return new TerritoryItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<TerritoryAddress> writer() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String now = simpleDateFormat.format(new Date());

        JdbcBatchItemWriter<TerritoryAddress> writer = new JdbcBatchItemWriter<TerritoryAddress>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<TerritoryAddress>());
        writer.setDataSource(dataSource);
        writer.setSql("INSERT INTO addresses (FirstName, LastName, Street, City, StateId, ZipCode, Notes, " +
                "OrderNumber, AddressStatusId, AddressStatusDate, AddressTypeId, DateCreated, CreatedBy, Deleted) " +
                "VALUES(:firstName, :lastName, :street, :city, :stateId, :zipCode, :notes, 0, 1, '" + now + "', " +
                "1, '" + now + "', 1, 0)");
        /*
        TODO: Don't insert if there's already a duplicate record
        writer.setSql("INSERT INTO addresses (FirstName, LastName, Street, City, StateId, ZipCode, Notes) " +
            "SELECT :firstName, :lastName, :street, :city, :stateId, :zipCode, :notes " +
            "FROM addresses " +
            "WHERE NOT EXISTS( "+
                "SELECT Street, City, StateId, ZipCode " +
                "FROM addresses " +
                "WHERE Street = :street " +
                "AND City = :city " +
                "AND StateId = :stateId " +
                "AND ZipCode = :zipCode " +
            ") " +
            "LIMIT 1;");
        */

        return writer;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importTerritoryAddressJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("importTerritoryAddressJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<TerritoryAddress, TerritoryAddress> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    // end::jobstep[]
}
