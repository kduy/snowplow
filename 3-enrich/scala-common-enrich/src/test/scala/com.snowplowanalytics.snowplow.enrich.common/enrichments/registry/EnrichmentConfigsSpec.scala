/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics
package snowplow
package enrich
package common
package enrichments
package registry

// Java
import java.net.URI
import java.lang.{Byte => JByte}

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s.jackson.JsonMethods.parse

// Iglu
import com.snowplowanalytics.iglu.client.SchemaKey

// Specs2
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers

/**
 * Tests enrichmentConfigs
 */
class EnrichmentConfigsSpec extends Specification with ValidationMatchers {

  "Parsing a valid anon_ip enrichment JSON" should {
    "successfully construct an AnonIpEnrichment case class" in {

      val ipAnonJson = parse("""{
        "enabled": true,
        "parameters": {
          "anonOctets": 2
        }
      }""")

      val schemaKey = SchemaKey("com.snowplowanalytics.snowplow", "anon_ip", "jsonschema", "1-0-0")

      val result = AnonIpEnrichment.parse(ipAnonJson, schemaKey)
      result must beSuccessful(AnonIpEnrichment(AnonOctets(2)))

    }
  }

  "Parsing a valid ip_lookups enrichment JSON" should {
    "successfully construct a GeoIpEnrichment case class" in {

      val ipToGeoJson = parse("""{
        "enabled": true,
        "parameters": {
          "geo": {
            "database": "GeoIPCity.dat",
            "uri": "http://snowplow-hosted-assets.s3.amazonaws.com/third-party/maxmind"
          },
          "isp": {
            "database": "GeoIPISP.dat",
            "uri": "http://snowplow-hosted-assets.s3.amazonaws.com/third-party/maxmind"            
          }
        }
      }""")

      val schemaKey = SchemaKey("com.snowplowanalytics.snowplow", "ip_lookups", "jsonschema", "1-0-0")

      val expected = IpLookupsEnrichment(Some("geo", new URI("http://snowplow-hosted-assets.s3.amazonaws.com/third-party/maxmind/GeoIPCity.dat"), "GeoIPCity.dat"),
                                         Some("isp", new URI("http://snowplow-hosted-assets.s3.amazonaws.com/third-party/maxmind/GeoIPISP.dat"), "GeoIPISP.dat"),
                                         None, None, None, true)

      val result = IpLookupsEnrichment.parse(ipToGeoJson, schemaKey, true)
      result must beSuccessful(expected)

    }
  }

  "Parsing a valid referer_parser enrichment JSON" should {
    "successfully construct a RefererParserEnrichment case class" in {

      val refererParserJson = parse("""{
        "enabled": true,
        "parameters": {
          "internalDomains": [
            "www.subdomain1.snowplowanalytics.com", 
            "www.subdomain2.snowplowanalytics.com"
          ]
        }
      }""")

      val schemaKey = SchemaKey("com.snowplowanalytics.snowplow", "referer_parser", "jsonschema", "1-0-0")

      val expected = RefererParserEnrichment(List("www.subdomain1.snowplowanalytics.com", "www.subdomain2.snowplowanalytics.com"))

      val result = RefererParserEnrichment.parse(refererParserJson, schemaKey)
      result must beSuccessful(expected)

    }      
  }

  "Parsing a valid campaign_attribution enrichment JSON" should {
    "successfully construct a CampaignAttributionEnrichment case class" in {

      val campaignAttributionEnrichmentJson = parse("""{
        "enabled": true,
        "parameters": {
          "mapping": "static",
          "fields": {
            "mktMedium": ["utm_medium", "medium"],
            "mktSource": ["utm_source", "source"],
            "mktTerm": ["utm_term"],
            "mktContent": [],
            "mktCampaign": ["utm _ campaign", "CID", "legacy-campaign!?-`@#$%^&*()=\\][}{/.,<>~|"]
          }
        }
      }""")

      val schemaKey = SchemaKey("com.snowplowanalytics.snowplow", "campaign_attribution", "jsonschema", "1-0-0")

      val expected = CampaignAttributionEnrichment(
        List("utm_medium", "medium"),
        List("utm_source", "source"),
        List("utm_term"),
        List(),
        List("utm _ campaign", "CID", "legacy-campaign!?-`@#$%^&*()=\\][}{/.,<>~|")
      )

      val result = CampaignAttributionEnrichment.parse(campaignAttributionEnrichmentJson, schemaKey)
      result must beSuccessful(expected)

    }      
  }

}
